@FilterDef(  // used for dynamic mapping
        name = Person.AGE_FILTER_NAME,
        parameters = { @ParamDef(name = Person.AGE_FILTER_ARGUMENT_NAME, type = "int")}
)
package com.hao.postgres.jpa.entity;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;