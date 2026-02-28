package io.renren.modules.plc.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.renren.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * PLC设备信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("plc_device")
public class PlcDeviceEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备编码（唯一）
     */
    private String deviceCode;

    /**
     * 协议类型：modbus-tcp、s7
     */
    private String protocol;

    /**
     * PLC连接地址（IP或主机名）
     */
    private String host;

    /**
     * 端口号（Modbus默认502，S7默认102）
     */
    private Integer port;

    /**
     * S7协议机架号（仅S7协议使用，默认0）
     */
    private Integer rack;

    /**
     * S7协议槽号（仅S7协议使用，默认0）
     */
    private Integer slot;

    /**
     * 设备状态：0-停用，1-启用
     */
    private Integer status;

    /**
     * 连接超时时间（毫秒，默认5000）
     */
    private Integer connectTimeout;

    /**
     * 备注
     */
    private String remark;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
}
