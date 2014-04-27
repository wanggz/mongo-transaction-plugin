package com.yudao.mongo.transaction;

import java.util.List;

import com.mongodb.DBCollection;

/**
 * 事务操作对象 <br>
 * 该对象为线程内共享对象 <br>
 * 
 * @author Austin
 * 
 */
public class Transaction
{

    /**事务记录表*/
    private DBCollection dbCollection;
    /**事务还原表*/
    private DBCollection dbRollBackCollection;
    // 事务次数
    private int transCount;
    // 提交次数
    private int commitCount;
    // 事务嵌套层次
    private int transDeep;
    // 事务记录
    private List<TransactionTable> transactionTables;

    public void setDbCollection(DBCollection dbCollection)
    {
        this.dbCollection = dbCollection;
    }

    public DBCollection getDbCollection()
    {
        return this.dbCollection;
    }

    public void setDbRollBackCollection(DBCollection dbRollBackCollection)
    {
        this.dbRollBackCollection = dbRollBackCollection;
    }

    public DBCollection getDbRollBackCollection()
    {
        return this.dbRollBackCollection;
    }

    public int getTransCount()
    {
        return this.transCount;
    }

    public void setTransCount(int transCount)
    {
        this.transCount = transCount;
    }

    public int getCommitCount()
    {
        return this.commitCount;
    }

    public void setCommitCount(int commitCount)
    {
        this.commitCount = commitCount;
    }

    public int getTransDeep()
    {
        return this.transDeep;
    }

    public void setTransDeep(int transDeep)
    {
        this.transDeep = transDeep;
    }

    public void setTransactionTables(List<TransactionTable> transactionTables)
    {
        this.transactionTables = transactionTables;
    }

    public List<TransactionTable> getTransactionTables()
    {
        return this.transactionTables;
    }

    /**
     * 判断事务是否完全提交。 通过提交次数和事务次数来判断事务是否完全提交。
     * 
     * @return
     */
    boolean hasFullExecute()
    {
        return this.commitCount + 1 == this.transCount;
    }
}
