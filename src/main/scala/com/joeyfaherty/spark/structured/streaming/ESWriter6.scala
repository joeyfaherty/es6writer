package com.joeyfaherty.spark.structured.streaming

import java.util.concurrent.TimeUnit

import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.{Dataset, SparkSession}

object ESWriter6 {

  case class Customer(id: String,
                      name: String,
                      age: Int,
                      isAdult: Boolean,
                      nowEpoch: Long)
  

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      // run app locally utilizing all cores
      .master("local[*]")
      .appName(getClass.getName)
      .config("es.nodes", "localhost")
      .config("es.port", "9200")
      .config("es.index.auto.create", "true") // this will ensure that index is also created on first POST
      .config("es.nodes.wan.only", "true") // needed to run against dockerized ES for local tests
      .config("es.net.http.auth.user", "elastic")
      .config("es.net.http.auth.pass", "changeme")
      .getOrCreate()

    import spark.implicits._

    val customerEvents: Dataset[Customer] = spark
      .readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", "5555")
      // maximum number of lines processed per trigger interval
      .option("maxFilesPerTrigger", 5)
      .load()
      .as[String]
      .map(line => {
        val cols = line.split(",")
        val age = cols(2).toInt
        Customer(cols(0), cols(1), age, age > 18, System.currentTimeMillis())
      })

    customerEvents
      .writeStream
      .outputMode(OutputMode.Append())
      .format("es")
      .option("checkpointLocation", "/tmp/checkpointLocation")
      .option("es.mapping.id", "id")
      .trigger(Trigger.ProcessingTime(5, TimeUnit.SECONDS))
      .start("customer/profile")
      .awaitTermination()
  }

}
