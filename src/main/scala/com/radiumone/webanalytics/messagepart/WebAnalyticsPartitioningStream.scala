package com.radiumone.webanalytics.messagepart

import java.util.Properties

import org.apache.kafka.common.serialization._
import org.apache.kafka.streams.kstream.KStreamBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.KStream


/**
  * Created by broy on 1/11/17.
  */
object WebAnalyticsPartitioningStream {

  def main(args: Array[String]): Unit = {

    if (args.length != 3) {
      System.err.println("Usage: "
        + this.getClass.getName
        + "sourceTopic destintationTopic applicationIdConfig")
      System.exit(1)
    }

    val Array(sourceTopic, destintationTopic, applicationIdConfig) = args

    val builder: KStreamBuilder = new KStreamBuilder

    val streamingConfig = {
      val settings = new Properties()
      settings.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationIdConfig)
      settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      settings.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "localhost:2181")
      settings.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray.getClass.getName)
      settings.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
      settings
    }

    val stringSerde: Serde[String] = Serdes.String()

    val textLines: KStream[Array[Byte], String] = builder.stream(sourceTopic)

//    val uppercasedWithMapValues: KStream[Array[Byte], String] = textLines.mapValues(_.toUpperCase())

//    partitioner: StreamPartitioner[K, V]
//    val partitioner = new StreamPartitioner[String, String]{}
    val appIdPartitioner = new AppIdPartitioner()

    textLines.to(appIdPartitioner, destintationTopic)

    val stream: KafkaStreams = new KafkaStreams(builder, streamingConfig)
    stream.start()

  }

}
