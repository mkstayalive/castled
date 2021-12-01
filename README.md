

# Introduction

Castled is a Reverse ETL tool which enables you to perodically sync the data from a **source**, the public cloud warehouse where you store all your data, to a **destination** which is your favorite operational tool there by enabling the sales,marketing or service teams.

Castled uses a **diff** computation logic for the data sync happening between your data warehouse and your destination app. This is done to make sure the computation happens in **ISOLATION** and **CONFINED** to your data warehouse. The diff computation process ensures it is not creating any kind of performance bottleneck at the source or the destination.It ensures that the database load stays with in limits at the source and at the same time the payload used to invoke the destination APIs are kept as light as possible.

The major components in a Reverse ETL Tool like Castled includes a **source** , a **destination** , the **data Models** created on the source , and the **pipelines** created for syncing the data from the source to the destination.

## Components

## Source (Data WareHouse)

Source for all the pipelines is a public warehouse, where you move all the data using the ETL/ELT tools. The emergence of the modern data stack gave the public data warehouses the much needed flexibility/scalability to mould or transform the data the way you need, making it the data hub. The modern cloud data warehouses enables you to do all kind of operations likes join, aggregate or even transform the data in the warehouse and use it not just for feeding BI tools but also use it for operational analytics.

Castled supports all the major cloud data warehouses including Snowflake, Redshift, BigQuery and PostgreSQL.

## Destination (App)

Any operational tool used by the sales,marketing or services team qualify to be a destination app.Use of Castled makes sure all the apps in your organisation are in sync and works with the same set of real time data.

Each destination is unique and will need unique connectors to sync with the source. The connectors provided by Castled are intuitive and makes sure the connection is established with least effort.Steps for using the connectors for each destination is covered in detail in the Destination section.

## Data Model

A data Model is an unique representation of the data in your data warehouse. Based on the requirement you can write a SQL QUERY joining/aggregating the tables/views in your data warehouse and is called a DATA MODEL. Castled provides an option to PREVIEW the data model there by assuring the user that the data to be synched is matching his requirements.

While creating a data model you need to make sure you have a column or a set of columns which uniquely identifies a row in the data model which matches with the unique identifier at the destination. This will be required while using UPSERT/UPDATE modes where you are updating an existing row at the destination.Defining an unique identifier at the source and destination makes the mapping process intuitive and error free.

## Data Pipeline

Data Pipeline forms the crux of Castled. Castled uses a clearly defined wizard to help you configure the pipeline. The major steps involved in creating a pipeline includes

- **Mapping the unique Identifier field**
  - The source and the destination needs to have an identifier field which uniquely identifies a row at the source and destination. This needs to be marked as primary identifier so that Castled use this field for uniquely identifying a data row and use it while UPSERT or UPDATE.
- **Mapping the remaining fields**
  - Once the unique identifier is mapped, you can map the remaining fields which needs to be synched from source to destination.
- **Sync Modes**
  - Sync modes decides how the data will be synced between the source and the destination.The supported sync modes include INSERT/UPSERT/UPDATE
    - **INSERT** mode is to be used when you want to add new records to the destination
    - **UPSERT** mode is to be used when you want to add new records or update the existing records.
    - **UPDATE** mode is to be used when you want to only update the existing records.
- **Sync Frequency**
  - This will help you select a frequency at which the data at the source is synced to the destination.

