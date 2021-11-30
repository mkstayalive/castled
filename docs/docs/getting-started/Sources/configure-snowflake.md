---
sidebar_position: 5
---

#  Snowflake

Snowflake is one of the most popular public cloud data warehouse which works on all major cloud providers including **Amazon Web Services** , **Google Cloud Platform** and **Microsoft Azure Cloud**

Castled enables you to fetch data from one or more tables spanning across one or more schemas in your Source Warehouse and sync it to the corresponding objects in the destination app.

Castled uses a **diff** computation logic for the data sync happening between your warehouse and destination app. This is done to make sure the computation happens in ISOLATION and CONFINING to your warehouse and is not creating any kind of performance bottleneck at the source as well as destination.This process makes sure
- The database load at the source stays well within limits
- The payload used to invoke the destination APIs are kept as light as possible.

## Permission Details
As a prerequisite access needs to be given to Castled for configuring Snowflake as a source.Snowflake permissions are complex and there are many ways to configure access for Castled. The script below is the minimum access configuration required and follows Snowflake's best practices for creating **read-only** roles in a role hierarchy.

The script below captures the minimal permissions required by CASTLED to use the data available in the Snowflake warehouse. Please refer Snowflake best practices for creating the required roles.

Scripts mentioned below covers the following
1. Creating a dedicated role **CASTLED_ROLE**
2. Creating a dedicated user **CASTLED**
3. Giving **READ ONLY** access to the user **CASTLED** to access all the current as well as future tables and views of the required schemas in your database
4. Creating a book keeping schema **CASTLED** with in your database and giving full access to that schema

```
-----------------------------------ROLE CREATION STARTS---------------------------------------------------------
-- Create a role for the CASTLED user
	CREATE ROLE CASTLED_ROLE;
-----------------------------------ROLE CREATION ENDS-----------------------------------------------------------


-----------------------------------USER CREATON STARTS----------------------------------------------------------
-- Create the CASTLED user
    CREATE USER CASTLED WITH DEFAULT_ROLE = CASTLED_ROLE PASSWORD = ‘<STRONG_PASSWORD>’;

-- Grant the role CASTLED_ROLE to the CASTLED user
    GRANT ROLE CASTLED_ROLE TO USER CASTLED;
-----------------------------------USER CREATON ENDS------------------------------------------------------------


-----------------------------------GRANT TABLE READ ONLY ACCESS STARTS------------------------------------------
-- Grant Usage on the warehouse to CASTLED_ROLE. This will allow the CASTLED user to run all the required queries in this warehouse.
 GRANT USAGE ON WAREHOUSE COMPUTE_WH TO ROLE CASTLED_ROLE;

-- Grant OPERATE privilege on the warehouse to CASTLED_ROLE. This is required to start/stop the warehouse and also to abort any running queries in the warehouse.
 GRANT OPERATE ON WAREHOUSE COMPUTE_WH TO ROLE CASTLED_ROLE;

-- Allow CASTLED_ROLE to access your database
GRANT USAGE ON DATABASE "<your_database>" TO ROLE CASTLED_ROLE;

-- Allow CASTLED_ROLE to access your schema
-- GRANT access to all the schemas used in the query, if the query spans multiple schemas
GRANT USAGE ON SCHEMA "<your_database>"."<your_schema>" TO ROLE CASTLED_ROLE;

-- Grant READ ONLY access to CASTLED_ROLE for all the existing tables in your schema
GRANT SELECT ON ALL TABLES IN SCHEMA "<your_database>"."<your_schema>" TO ROLE CASTLED_ROLE;

-- Grant READ ONLY access to CASTLED_ROLE for all the future tables created in your schema
GRANT SELECT ON FUTURE TABLES IN SCHEMA "<your_database>"."<your_schema>" TO ROLE CASTLED_ROLE;

-- Grant READ ONLY access to CASTLED_ROLE for all the existing views in your schema
GRANT SELECT ON ALL VIEWS IN SCHEMA "<your_database>"."<your_schema>" TO ROLE CASTLED_ROLE;

-- Grant READ ONLY access to CASTLED_ROLE for all the future views created in your schema
GRANT SELECT ON FUTURE VIEWS IN SCHEMA "<your_database>"."<your_schema>" TO ROLE CASTLED_ROLE;

-- Grant READ ONLY access to CASTLED_ROLE for all the existing functions in your schema
GRANT USAGE ON ALL FUNCTIONS IN SCHEMA "<your_database>"."<your_schema>" TO ROLE CASTLED_ROLE;

-- Grant READ ONLY access to CASTLED_ROLE for all the future functions created in your schema
GRANT USAGE ON FUTURE FUNCTIONS IN SCHEMA "<your_database>"."<your_schema>" TO ROLE CASTLED_ROLE;
-----------------------------------GRANT TABLE READ ONLY ACCESS ENDS--------------------------------------------


-----------------------------------CREATE AND GRANT BOOK KEEPING ACCESS STARTS----------------------------------
-- Create a book keeping schema CASTLED to store sync state
 CREATE SCHEMA "<your_database>"."CASTLED";

-- Give the CASTLED_ROLE role full access to the book keeping schema
  GRANT ALL PRIVILEGES ON SCHEMA "<your_database>"."CASTLED" TO ROLE CASTLED_ROLE;
-----------------------------------CREATE AND GRANT BOOK KEEPING ACCESS ENDS-------------------------------------

```
## Connector Details

**For configuring a new connector for Snowflake the following fields needs to be captured**
- **Name**
    - A name to uniquely qualify the warehouse source created 
- **Warehouse Name**
    - Name of the warehouse 
- **Account Locator**
    - An account locator is an identifier assigned by Snowflake when the account is created
- **Region**
    - Cloud region dictated by the Cloud Platform
- **Cloud**
    - Cloud Platform identifier
- **Database name**
    - Name of database connected
- **Schema Name**
    - Schema name to be used.If the schema is mentioned here it can be avoided in the query
- **Database username**
    - Database username
- **Database Password**
    - Database password

![Docusaurus](/img/screens/sources/snowflake/config_snowflake.png)



