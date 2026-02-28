package io.renren.modules.plc.service.impl;

import io.renren.common.exception.RenException;
import io.renren.modules.plc.dto.PlcReadRequestDTO;
import io.renren.modules.plc.dto.PlcWriteRequestDTO;
import io.renren.modules.plc.entity.PlcDeviceEntity;
import io.renren.modules.plc.service.PlcCommunicationService;
import io.renren.modules.plc.service.PlcDeviceService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.PlcDriverManager;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * PLC通讯服务实现
 * <p>
 * 基于 Apache PLC4X (PLC4J) 框架实现，内部维护连接缓存以复用连接。
 * 支持协议：modbus-tcp（Modbus TCP）、s7（Siemens S7）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlcCommunicationServiceImpl implements PlcCommunicationService {

    private final PlcDeviceService plcDeviceService;

    /**
     * 连接缓存，key为deviceId
     */
    private final Map<Long, PlcConnection> connectionCache = new ConcurrentHashMap<>();

    /**
     * 读取操作超时时间（秒）
     */
    private static final long READ_TIMEOUT_SECONDS = 5;

    /**
     * 写入操作超时时间（秒）
     */
    private static final long WRITE_TIMEOUT_SECONDS = 5;

    @Override
    public Map<String, Object> read(PlcReadRequestDTO request) {
        PlcDeviceEntity device = getEnabledDevice(request.getDeviceId());
        PlcConnection connection = getOrCreateConnection(device);

        if (!connection.getMetadata().canRead()) {
            throw new RenException("该PLC设备不支持读取操作，协议：" + device.getProtocol());
        }

        PlcReadRequest.Builder builder = connection.readRequestBuilder();
        for (PlcReadRequestDTO.PlcTagDTO tag : request.getTags()) {
            builder.addTagAddress(tag.getTagName(), tag.getTagAddress());
        }
        PlcReadRequest readRequest = builder.build();

        try {
            PlcReadResponse response = readRequest.execute().get(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Map<String, Object> result = new HashMap<>();
            for (String tagName : response.getTagNames()) {
                if (response.getResponseCode(tagName) == PlcResponseCode.OK) {
                    PlcValue plcValue = response.getPlcValue(tagName);
                    result.put(tagName, extractPlcValue(plcValue));
                } else {
                    result.put(tagName, "ERROR:" + response.getResponseCode(tagName).name());
                }
            }
            return result;
        } catch (Exception e) {
            log.error("PLC读取失败，设备[{}]：{}", device.getDeviceCode(), e.getMessage(), e);
            invalidateConnection(device.getId());
            throw new RenException("PLC读取失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, String> write(PlcWriteRequestDTO request) {
        PlcDeviceEntity device = getEnabledDevice(request.getDeviceId());
        PlcConnection connection = getOrCreateConnection(device);

        if (!connection.getMetadata().canWrite()) {
            throw new RenException("该PLC设备不支持写入操作，协议：" + device.getProtocol());
        }

        PlcWriteRequest.Builder builder = connection.writeRequestBuilder();
        for (PlcWriteRequestDTO.PlcWriteTagDTO tag : request.getTags()) {
            Object value = convertValue(tag.getValue(), tag.getDataType());
            builder.addTagAddress(tag.getTagName(), tag.getTagAddress(), value);
        }
        PlcWriteRequest writeRequest = builder.build();

        try {
            PlcWriteResponse response = writeRequest.execute().get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Map<String, String> result = new HashMap<>();
            for (String tagName : response.getTagNames()) {
                result.put(tagName, response.getResponseCode(tagName).name());
            }
            return result;
        } catch (Exception e) {
            log.error("PLC写入失败，设备[{}]：{}", device.getDeviceCode(), e.getMessage(), e);
            invalidateConnection(device.getId());
            throw new RenException("PLC写入失败：" + e.getMessage());
        }
    }

    @Override
    public boolean testConnection(Long deviceId) {
        PlcDeviceEntity device = getEnabledDevice(deviceId);
        // 强制关闭缓存连接，重新建立
        closeConnection(deviceId);
        try {
            PlcConnection connection = createConnection(device);
            connectionCache.put(deviceId, connection);
            return connection.isConnected();
        } catch (Exception e) {
            log.warn("PLC连接测试失败，设备[{}]：{}", device.getDeviceCode(), e.getMessage());
            return false;
        }
    }

    @Override
    public void closeConnection(Long deviceId) {
        PlcConnection connection = connectionCache.remove(deviceId);
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.warn("关闭PLC连接时发生异常，deviceId={}：{}", deviceId, e.getMessage());
            }
        }
    }

    /**
     * 获取或创建PLC连接
     */
    private PlcConnection getOrCreateConnection(PlcDeviceEntity device) {
        PlcConnection connection = connectionCache.get(device.getId());
        if (connection != null && connection.isConnected()) {
            return connection;
        }
        // 旧连接已断开，移除并重建
        invalidateConnection(device.getId());
        connection = createConnection(device);
        connectionCache.put(device.getId(), connection);
        return connection;
    }

    /**
     * 根据设备配置创建PLC连接
     */
    private PlcConnection createConnection(PlcDeviceEntity device) {
        String connectionUrl = buildConnectionUrl(device);
        log.info("正在连接PLC设备[{}]，URL：{}", device.getDeviceCode(), connectionUrl);
        try {
            PlcConnection connection = PlcDriverManager.getDefault().getConnection(connectionUrl);
            if (!connection.isConnected()) {
                throw new RenException("PLC连接建立后状态为未连接，URL：" + connectionUrl);
            }
            log.info("PLC设备[{}]连接成功", device.getDeviceCode());
            return connection;
        } catch (RenException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建PLC连接失败，设备[{}]：{}", device.getDeviceCode(), e.getMessage(), e);
            throw new RenException("创建PLC连接失败：" + e.getMessage());
        }
    }

    /**
     * 根据协议和设备配置构建PLC4X连接URL
     * <p>
     * Modbus TCP: modbus-tcp://host:port
     * Siemens S7: s7://host/rack/slot
     */
    private String buildConnectionUrl(PlcDeviceEntity device) {
        String protocol = device.getProtocol().toLowerCase();
        String host = device.getHost();

        switch (protocol) {
            case "modbus-tcp": {
                int port = device.getPort() != null ? device.getPort() : 502;
                return String.format("modbus-tcp://%s:%d", host, port);
            }
            case "s7": {
                int port = device.getPort() != null ? device.getPort() : 102;
                int rack = device.getRack() != null ? device.getRack() : 0;
                int slot = device.getSlot() != null ? device.getSlot() : 0;
                return String.format("s7://%s:%d/%d/%d", host, port, rack, slot);
            }
            default:
                throw new RenException("不支持的PLC协议类型：" + device.getProtocol()
                        + "，当前支持：modbus-tcp、s7");
        }
    }

    /**
     * 使缓存中的连接失效并关闭
     */
    private void invalidateConnection(Long deviceId) {
        PlcConnection old = connectionCache.remove(deviceId);
        if (old != null) {
            try {
                old.close();
            } catch (Exception e) {
                log.debug("关闭旧PLC连接时发生异常：{}", e.getMessage());
            }
        }
    }

    /**
     * 查询并校验设备是否存在且已启用
     */
    private PlcDeviceEntity getEnabledDevice(Long deviceId) {
        PlcDeviceEntity device = plcDeviceService.selectById(deviceId);
        if (device == null) {
            throw new RenException("PLC设备不存在，id=" + deviceId);
        }
        if (device.getStatus() == null || device.getStatus() != 1) {
            throw new RenException("PLC设备已停用，设备编码：" + device.getDeviceCode());
        }
        return device;
    }

    /**
     * 将PLC值转换为Java对象（优先返回具体类型）
     */
    private Object extractPlcValue(PlcValue plcValue) {
        if (plcValue == null) {
            return null;
        }
        try {
            if (plcValue.isBoolean()) {
                return plcValue.getBoolean();
            } else if (plcValue.isInteger()) {
                return plcValue.getInteger();
            } else if (plcValue.isLong()) {
                return plcValue.getLong();
            } else if (plcValue.isFloat()) {
                return plcValue.getFloat();
            } else if (plcValue.isDouble()) {
                return plcValue.getDouble();
            } else if (plcValue.isString()) {
                return plcValue.getString();
            } else {
                return plcValue.getString();
            }
        } catch (Exception e) {
            return plcValue.getString();
        }
    }

    /**
     * 将字符串值按数据类型转换为对应的Java对象
     */
    private Object convertValue(String value, String dataType) {
        if (dataType == null || dataType.isBlank()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return value;
            }
        }
        switch (dataType.toUpperCase()) {
            case "BOOL":
                return "1".equals(value.trim()) || Boolean.parseBoolean(value.trim());
            case "INT":
                return Integer.parseInt(value);
            case "DINT":
                return Long.parseLong(value);
            case "REAL":
            case "FLOAT":
                return Float.parseFloat(value);
            case "LREAL":
            case "DOUBLE":
                return Double.parseDouble(value);
            case "STRING":
                return value;
            default:
                return value;
        }
    }

    /**
     * 应用关闭时，关闭所有缓存的PLC连接
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭所有PLC连接，共{}个", connectionCache.size());
        connectionCache.forEach((deviceId, connection) -> {
            try {
                connection.close();
                log.info("已关闭PLC连接，deviceId={}", deviceId);
            } catch (Exception e) {
                log.warn("关闭PLC连接时发生异常，deviceId={}：{}", deviceId, e.getMessage());
            }
        });
        connectionCache.clear();
    }
}
