package io.renren.modules.plc.service;

import io.renren.common.page.PageData;
import io.renren.common.service.BaseService;
import io.renren.modules.plc.entity.PlcDeviceEntity;
import io.renren.modules.plc.dto.PlcDeviceDTO;

import java.util.Map;

/**
 * PLC设备管理服务接口
 */
public interface PlcDeviceService extends BaseService<PlcDeviceEntity> {

    /**
     * 分页查询PLC设备列表
     */
    PageData<PlcDeviceDTO> page(Map<String, Object> params);

    /**
     * 根据ID查询PLC设备
     */
    PlcDeviceDTO get(Long id);

    /**
     * 保存PLC设备
     */
    void save(PlcDeviceDTO dto);

    /**
     * 更新PLC设备
     */
    void update(PlcDeviceDTO dto);

    /**
     * 批量删除PLC设备
     */
    void deleteBatch(Long[] ids);
}
