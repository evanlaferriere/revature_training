package com.Revature
import scala.io.Source
import java.sql.DriverManager
import java.sql.Connection
import scala.util.control.Breaks._

import scala.io.StdIn
import scala.util.matching.Regex

import java.sql.PreparedStatement
import java.sql.SQLException
import javax.sql.DataSource

object bakery_game {
  val driver = "com.mysql.cj.jdbc.Driver"
  val url = "jdbc:mysql://localhost:3306/Bakery"
  val username = "root"
  val password = "Fuck74!MySQL"
  var connection:Connection = DriverManager.getConnection(url, username, password)
  val statement = connection.createStatement()

  def main(args: Array[String]): Unit = {
    refresh()
    println("Hello baker!")
    var gameLoop = true
    while (gameLoop) {
      kitchenStocks()
      var whatCook = knownRecipes()
      println("What do you want to make? (Type the recipe number) ")
      var recipe: String = readLine()
      if (recipe.toLowerCase == "quit") {gameLoop = false}
      else {
        if(recipe forall Character.isDigit){
          println("Okay, that will take: ")
          recipeWillTake(recipe)
          println("Are you sure? Y/n")
        }
      }
    }
    connection.close()
  }
  def refresh(): Unit={

    val trunc1 = statement.executeUpdate("TRUNCATE food;")
    val trunc2 = statement.executeUpdate("TRUNCATE stock;")

    val foodList = Source.fromFile("C:\\Users\\15084\\Desktop\\Revature\\bakery\\project0\\src\\main\\scala\\foods.json").getLines.toList
    val stockList =   Source.fromFile("C:\\Users\\15084\\Desktop\\Revature\\bakery\\project0\\src\\main\\scala\\stocks.json").getLines.toList

    val nums = "[0-9]+".r
    val words = "[a-z]+".r

    foodList.foreach{x => var insert = statement.executeUpdate("INSERT INTO FOOD(id, foodName) VALUES("+nums.findFirstMatchIn(x).get+", \'"+words.findFirstMatchIn(x).get+"\');")}
    stockList.foreach{x => var insert = statement.executeUpdate("INSERT INTO stock(id, quantity) VALUES("+nums.findAllIn(x).toList.apply(0)+", "+nums.findAllIn(x).toList.apply(1)+");")}
  }
  def kitchenStocks(): Unit={
    println("Here are your kitchen stocks: ")
    val resultSet3 = statement.executeQuery("SELECT food.foodName, stock.quantity FROM stock JOIN food on food.id = stock.id;")
    while (resultSet3.next()) {
      println(resultSet3.getString(1) + ", " + resultSet3.getString(2))
    }
  }
  def knownRecipes(): Unit={
    println("And here are the recipes you know: ")
    val resultSet2 = statement.executeQuery("SELECT id, recipeName FROM recipe;")
    while (resultSet2.next()) {
      println(resultSet2.getString(1) + ", " + resultSet2.getString(2))
    }
  }
  def recipeWillTake(recipe: String): Unit={
    val resultSet4 = statement.executeQuery("select * from recipe join food ON (recipe.ingr1=food.id OR recipe.ingr2=food.id OR recipe.ingr3=food.id OR recipe.ingr4=food.id OR recipe.ingr5=food.id) AND recipe.id=" + recipe.toInt + ";")
    var count = 0
    while (resultSet4.next()) {
      println(resultSet4.getString(4 + count) + " " + resultSet4.getString(14))
      count += 2
    }
  }
}
