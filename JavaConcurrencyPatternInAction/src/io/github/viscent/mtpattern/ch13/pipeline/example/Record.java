/*
授权声明：
本源码系《Java多线程编程实战指南（设计模式篇）第2版》一书（ISBN：978-7-121-38245-1，以下称之为“原书”）的配套源码，
欲了解本代码的更多细节，请参考原书。
本代码仅为原书的配套说明之用，并不附带任何承诺（如质量保证和收益）。
以任何形式将本代码之部分或者全部用于营利性用途需经版权人书面同意。
将本代码之部分或者全部用于非营利性用途需要在代码中保留本声明。
任何对本代码的修改需在代码中以注释的形式注明修改人、修改时间以及修改内容。
本代码可以从以下网址下载：
https://github.com/Viscent/javamtp
http://www.broadview.com.cn/38245
*/

package io.github.viscent.mtpattern.ch13.pipeline.example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class Record {
    // 2014-08-10 12:58:08.0
    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss.S");
    private static final Pattern PATTERN_COMMA = Pattern.compile(",");
    private int id;
    private String productId;
    private String packageId;
    private String msisdn;

    private Date operationTime;
    private Date effectiveDate;
    private Date dueDate;

    private int operationType;

    public int targetFileIndex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Record other = (Record) obj;
        if (id != other.id)
            return false;
        return true;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public static Record parseCsv(String line) throws ParseException {
        String[] arr = PATTERN_COMMA.split(line);
        Record ret = new Record();
        // ID PRODUCTID PACKAGEID MSISDN OPERATIONTIME EFFECTIVEDATE DUEDATE
        // OPERATIONTYPE
        ret.setId(Integer.valueOf(arr[0]));
        ret.setProductId(arr[1]);
        ret.setPackageId(arr[2]);
        ret.setMsisdn(arr[3]);
        ret.setOperationTime(sdf.parse(arr[4]));
        ret.setEffectiveDate(sdf.parse(arr[5]));
        ret.setDueDate(sdf.parse(arr[6]));
        ret.setOperationType(Integer.valueOf(arr[7]));
        return ret;
    }

    @Override
    public String toString() {
        return "Record [id=" + id + ", productId=" + productId + ", packageId="
                + packageId + ", msisdn=" + msisdn + ", operationTime="
                + operationTime
                + ", effectiveDate=" + effectiveDate + ", dueDate=" + dueDate
                + ", operationType=" + operationType + "]";
    }

}