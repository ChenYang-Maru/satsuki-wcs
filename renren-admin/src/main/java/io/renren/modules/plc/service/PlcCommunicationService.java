package io.renren.modules.plc.service;

import io.renren.modules.plc.dto.PlcReadRequestDTO;
import io.renren.modules.plc.dto.PlcWriteRequestDTO;

import java.util.Map;

/**
 * PLC通讯服务接口
 * <p>
 * 基于 Apache PLC4X (PLC4J) 框架，支持 Modbus TCP 和 Siemens S7 协议。
 * <p>
 * Modbus TCP 标签地址格式：
 *   线圈(Coil):            0x{地址}:BOOL          例: 0x000001:BOOL
 *   离散输入(Discrete Input):  1x{地址}:BOOL       例: 1x000001:BOOL
 *   保持寄存器(Holding Reg):   4x{地址}:INT        例: 4x000001:INT
 *   输入寄存器(Input Reg):     3x{地址}:INT        例: 3x000001:INT
 * <p>
 * Siemens S7 标签地址格式：
 *   数据块:  %DB{块号}.DB{类型}{字节偏移}:{类型}    例: %DB1.DBW0:INT
 *   标志位:  %M{字节}.{位}:BOOL                   例: %M0.0:BOOL
 *   输入:    %I{字节}.{位}:BOOL                   例: %I0.0:BOOL
 *   输出:    %Q{字节}.{位}:BOOL                   例: %Q0.0:BOOL
 */
public interface PlcCommunicationService {

    /**
     * 从PLC读取数据
     *
     * @param request 读取请求，包含设备ID和标签列表
     * @return 标签名称 -> 读取值的映射
     */
    Map<String, Object> read(PlcReadRequestDTO request);

    /**
     * 向PLC写入数据
     *
     * @param request 写入请求，包含设备ID和标签值列表
     * @return 标签名称 -> 写入结果(SUCCESS/NOT_FOUND/ACCESS_DENIED)的映射
     */
    Map<String, String> write(PlcWriteRequestDTO request);

    /**
     * 测试与PLC设备的连接
     *
     * @param deviceId 设备ID
     * @return true表示连接成功
     */
    boolean testConnection(Long deviceId);

    /**
     * 关闭指定设备的连接（连接池清理）
     *
     * @param deviceId 设备ID
     */
    void closeConnection(Long deviceId);
}
