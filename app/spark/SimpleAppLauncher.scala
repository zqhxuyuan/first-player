package spark

import org.apache.spark.launcher.{SparkAppHandle, SparkLauncher}
;

/**
  * 本地开发环境,使用SparkLauncher,运行作业,提交到mesos
  */
object SimpleAppLauncher {

  def main(args: Array[String]) {
    val spark = new SparkLauncher()
      .setAppResource("/Users/zhengqh/Github/first-player/target/universal/first-player-1.0-SNAPSHOT/lib/first-player.first-player-1.0-SNAPSHOT-sans-externalized.jar")
      .setMainClass("spark.SimpleApp.main")
      //.setMaster("mesos://192.168.6.52:5050")
      .setMaster("local[2]")
      .setSparkHome("/Users/zhengqh/soft/spark-1.6.2-bin-hadoop2.6")
      .setConf(SparkLauncher.DRIVER_MEMORY, "1g")
      .launch();
    spark.waitFor();
    println("success")

    val handle = new SparkLauncher()
      .setAppResource("/Users/zhengqh/Github/first-player/target/universal/first-player-1.0-SNAPSHOT/lib/first-player.first-player-1.0-SNAPSHOT-sans-externalized.jar")
      .setMainClass("spark.SimpleAppXXX")
      .setMaster("local")
      .setSparkHome("/Users/zhengqh/soft/spark-1.6.2-bin-hadoop2.6")
      .setConf(SparkLauncher.DRIVER_MEMORY, "2g")
      .startApplication();
    println("success..")
  }
}