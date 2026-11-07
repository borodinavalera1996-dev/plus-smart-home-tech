package ru.yandex.practicum.model.hub;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;

import java.util.Objects;

/**
 * Условие сценария, которое содержит информацию о датчике, типе условия, операции и значении.
 */

@Schema(name = "ScenarioCondition", description = "Условие сценария, которое содержит информацию о датчике, типе условия, операции и значении.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-11-02T00:12:43.626220561+03:00[Europe/Moscow]", comments = "Generator version: 7.10.0")
public class ScenarioCondition {

  private String sensorId;

  /**
   * Типы условий, которые могут использоваться в сценариях.
   */
  public enum TypeEnum {
    MOTION("MOTION"),
    
    LUMINOSITY("LUMINOSITY"),
    
    SWITCH("SWITCH"),
    
    TEMPERATURE("TEMPERATURE"),
    
    CO2_LEVEL("CO2LEVEL"),
    
    HUMIDITY("HUMIDITY");

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

  /**
   * Операции, которые могут быть использованы в условиях.
   */
  public enum OperationEnum {
    EQUALS("EQUALS"),
    
    GREATER_THAN("GREATER_THAN"),
    
    LOWER_THAN("LOWER_THAN");

    private String value;

    OperationEnum(String value) {
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
    public static OperationEnum fromValue(String value) {
      for (OperationEnum b : OperationEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private OperationEnum operation;

  private Integer value;

  public ScenarioCondition sensorId(String sensorId) {
    this.sensorId = sensorId;
    return this;
  }

  /**
   * Идентификатор датчика, связанного с условием.
   * @return sensorId
   */
  
  @Schema(name = "sensorId", description = "Идентификатор датчика, связанного с условием.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sensorId")
  public String getSensorId() {
    return sensorId;
  }

  public void setSensorId(String sensorId) {
    this.sensorId = sensorId;
  }

  public ScenarioCondition type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Типы условий, которые могут использоваться в сценариях.
   * @return type
   */
  
  @Schema(name = "type", description = "Типы условий, которые могут использоваться в сценариях.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public ScenarioCondition operation(OperationEnum operation) {
    this.operation = operation;
    return this;
  }

  /**
   * Операции, которые могут быть использованы в условиях.
   * @return operation
   */
  
  @Schema(name = "operation", description = "Операции, которые могут быть использованы в условиях.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("operation")
  public OperationEnum getOperation() {
    return operation;
  }

  public void setOperation(OperationEnum operation) {
    this.operation = operation;
  }

  public ScenarioCondition value(Integer value) {
    this.value = value;
    return this;
  }

  /**
   * Значение, используемое в условии.
   * @return value
   */
  
  @Schema(name = "value", description = "Значение, используемое в условии.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
    ScenarioCondition scenarioCondition = (ScenarioCondition) o;
    return Objects.equals(this.sensorId, scenarioCondition.sensorId) &&
        Objects.equals(this.type, scenarioCondition.type) &&
        Objects.equals(this.operation, scenarioCondition.operation) &&
        Objects.equals(this.value, scenarioCondition.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sensorId, type, operation, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScenarioCondition {\n");
    sb.append("    sensorId: ").append(toIndentedString(sensorId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
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

