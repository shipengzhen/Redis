/**
 * @文件名称： Student.java
 * @文件路径： com.bdqn.spz.spring.redis.pojo
 * @功能描述： TODO
 * @作者： shipengzhen
 * @创建时间：2018年7月24日 下午4:09:26
 */
package com.bdqn.spz.spring.redis.pojo;

import java.io.Serializable;

/**
 * @功能描述：
 * @创建人： shipengzhen
 * @创建时间： 2018年7月24日 下午4:09:26
 */
public class Student implements Serializable {

    /**
     * @字段名 long serialVersionUID
     * @功能描述 TODO
     * @创建人 shipengzhen
     * @创建时间 2018年7月24日下午4:10:31
     */
    private static final long serialVersionUID = -3944958428935529952L;

    public String name;
    
    public int age;
    
    public String sex;

    /**
     * @方法名 getName
     * @param String name
     * @return String
     * @功能描述 TODO
     * @创建人 shipengzhen
     * @创建时间 2018年7月24日下午4:10:10
     */
    public String getName() {
        return name;
    }

    /**
     * @方法名 setName
     * @param String name
     * @功能描述 TODO
     * @创建人 shipengzhen 
     * @创建时间 2018年7月24日下午4:10:10
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @方法名 getAge
     * @param int age
     * @return int
     * @功能描述 TODO
     * @创建人 shipengzhen
     * @创建时间 2018年7月24日下午4:10:10
     */
    public int getAge() {
        return age;
    }

    /**
     * @方法名 setAge
     * @param int age
     * @功能描述 TODO
     * @创建人 shipengzhen 
     * @创建时间 2018年7月24日下午4:10:10
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @方法名 getSex
     * @param String sex
     * @return String
     * @功能描述 TODO
     * @创建人 shipengzhen
     * @创建时间 2018年7月24日下午4:10:10
     */
    public String getSex() {
        return sex;
    }

    /**
     * @方法名 setSex
     * @param String sex
     * @功能描述 TODO
     * @创建人 shipengzhen 
     * @创建时间 2018年7月24日下午4:10:10
     */
    public void setSex(String sex) {
        this.sex = sex;
    }
}
