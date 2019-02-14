package com.sc.fe.analyze.data.entity;

import io.swagger.annotations.ApiModel;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@ApiModel(value = "Extensions", description = "Table Structure of Extensions")
@Table(value = "extensions")
public class Extensions {

    @PrimaryKey
    private int id;
    private String name;

    /**
     * Gets the Id.
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id into the id variable.
     *
     * @param id sets the id.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name into the name variable.
     *
     * @param name sets the name.
     */
    public void setName(String name) {
        this.name = name;
    }

}
