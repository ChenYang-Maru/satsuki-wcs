package io.renren.modules.plc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * PLC设备DTO
 */
@Data
@Schema(title = "PLC设备")
public class PlcDeviceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(title = "id")
    @Null(message = "{id.null}", groups = AddGroup.class)
    @NotNull(message = "{id.require}", groups = UpdateGroup.class)
    private Long id;

    @Schema(title = "设备名称")
    @NotBlank(message = "设备名称不能为空", groups = DefaultGroup.class)
    private String deviceName;

    @Schema(title = "设备编码（唯一）")
    @NotBlank(message = "设备编码不能为空", groups = DefaultGroup.class)
    private String deviceCode;

    @Schema(title = "协议类型：modbus-tcp、s7")
    @NotBlank(message = "协议类型不能为空", groups = DefaultGroup.class)
    private String protocol;

    @Schema(title = "PLC连接地址（IP或主机名）")
    @NotBlank(message = "连接地址不能为空", groups = DefaultGroup.class)
    private String host;

    @Schema(title = "端口号（Modbus默认502，S7默认102）")
    private Integer port;

    @Schema(title = "S7协议机架号（默认0）")
    private Integer rack;

    @Schema(title = "S7协议槽号（默认0）")
    private Integer slot;

    @Schema(title = "设备状态：0-停用，1-启用")
    private Integer status;

    @Schema(title = "连接超时时间（毫秒，默认5000）")
    private Integer connectTimeout;

    @Schema(title = "备注")
    private String remark;

    @Schema(title = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date createDate;

    @Schema(title = "更新时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date updateDate;
}
