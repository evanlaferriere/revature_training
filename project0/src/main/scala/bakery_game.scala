package com.Revature
import scala.io.Source
import java.sql.DriverManager
import java.sql.Connection
import scala.util.matching.Regex

import java.sql.PreparedStatement
import java.sql.SQLException
import javax.sql.DataSource

object bakery_game {

  def main(args: Array[String]): Unit = {

    val driver = "com.mysql.cj.jdbc.Driver"
    val url = "jdbc:mysql://localhost:3306/Bakery"
    val username = "root"
    val password = "Fuck74!MySQL"
    var connection:Connection = DriverManager.getConnection(url, username, password)
    val statement = connection.createStatement()

    val trunc1 = statement.executeUpdate("TRUNCATE food;")
    val trunc2 = statement.executeUpdate("TRUNCATE stock;")

    val foodList = Source.fromFile("C:\\Users\\15084\\Desktop\\Revature\\bakery\\project0\\src\\main\\scala\\foods.json").getLines.toList
    val stockList =   Source.fromFile("C:\\Users\\15084\\Desktop\\Revature\\bakery\\project0\\src\\main\\scala\\stocks.json").getLines.toList

    val nums = "[0-9]+".r
    val words = "[a-z]+".r

    foodList.foreach{x => var insert = statement.executeUpdate("INSERT INTO FOOD(id, foodName) VALUES("+nums.findFirstMatchIn(x).get+", \'"+words.findFirstMatchIn(x).get+"\');")}
    stockList.foreach{x => var insert = statement.executeUpdate("INSERT INTO stock(id, quantity) VALUES("+nums.findAllIn(x).toList.apply(0)+", "+nums.findAllIn(x).toList.apply(1)+");")}

    println("Hello baker! Here are your kitchen stocks: ")
    val resultSet3 = statement.executeQuery("SELECT food.foodName, stock.quantity FROM stock JOIN food on food.id = stock.id;")
    while ( resultSet3.next() ) {
      println(resultSet3.getString(1)+", " +resultSet3.getString(2))
    }


    connection.close()
  }
}
