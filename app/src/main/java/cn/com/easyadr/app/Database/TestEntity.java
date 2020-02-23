package cn.com.easyadr.app.Database;

import cn.com.easyadr.database.BaseEntity;
import cn.com.easyadr.database.annotation.Column;
import cn.com.easyadr.database.annotation.Table;

@Table("test_t")
public class TestEntity extends BaseEntity {

    @Column("f_int")
    private Integer intValue;

    @Column("f_long")
    private Long longValue;

    @Column("f_bool")
    private Boolean boolValue;

    @Column("f_float")
    private Float floatValue;

    @Column("f_double")
    private Double doubleValue;

    @Column("f_string")
    private String stringValue;

    @Column("f_blob")
    private byte[] blobValue;

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Boolean getBoolValue() {
        return boolValue;
    }

    public void setBoolValue(Boolean boolValue) {
        this.boolValue = boolValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(Float floatValue) {
        this.floatValue = floatValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public byte[] getBlobValue() {
        return blobValue;
    }

    public void setBlobValue(byte[] blobValue) {
        this.blobValue = blobValue;
    }
}
