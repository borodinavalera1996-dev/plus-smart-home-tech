package ru.yandex.practicum.model.hub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;

import java.util.Objects;

/**
 * Представляет действие, которое должно быть выполнено устройством.
 */

@Schema(name = "DeviceAction", description = "Представляет действие, которое должно быть выполнено устройством.")
public class DeviceAction {

  private String sensorId;

  /**
   * Перечисление возможных типов действий при срабатывании условия активации сценария.
   */
  public enum TypeEnum {
    ACTIVATE("ACTIVATE"),
    
    DEACTIVATE("DEACTIVATE"),
    
    INVERSE("INVERSE"),
    
    SET_VALUE("SET_VALUE");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private TypeEnum type;

  private Integer value;

  public DeviceAction sensorId(String sensorId) {
    this.sensorId = sensorId;
    return this;
  }

  /**
   * Идентификатор датчика, связанного с действием.
   * @return sensorId
   */
  
  @Schema(name = "sensorId", description = "Идентификатор датчика, связанного с действием.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sensorId")
  public String getSensorId() {
    return sensorId;
  }

  public void setSensorId(String sensorId) {
    this.sensorId = sensorId;
  }

  public DeviceAction type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Перечисление возможных типов действий при срабатывании условия активации сценария.
   * @return type
   */
  
  @Schema(name = "type", description = "Перечисление возможных типов действий при срабатывании условия активации сценария.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public DeviceAction value(Integer value) {
    this.value = value;
    return this;
  }

  /**
   * Необязательное значение, связанное с действием.
   * @return value
   */
  
  @Schema(name = "value", description = "Необязательное значение, связанное с действием.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("value")
  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeviceAction deviceAction = (DeviceAction) o;
    return Objects.equals(this.sensorId, deviceAction.sensorId) &&
        Objects.equals(this.type, deviceAction.type) &&
        Objects.equals(this.value, deviceAction.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sensorId, type, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceAction {\n");
    sb.append("    sensorId: ").append(toIndentedString(sensorId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

