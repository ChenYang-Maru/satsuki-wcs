package io.renren.modules.plc.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.plc.entity.PlcDeviceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * PLC设备DAO
 */
@Mapper
public interface PlcDeviceDao extends BaseDao<PlcDeviceEntity> {
}
