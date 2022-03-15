package com.Revature
import scala.io.Source
import java.sql.DriverManager
import java.sql.Connection
import scala.util.Random
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
  val randy = new Random()

  def main(args: Array[String]): Unit = {
    refresh()
    println("Hello baker!")
    var money = 0
    var gameLoop = true
    while (gameLoop) {
      println("You have $"+money+'.')
      kitchenStocks()
      knownRecipes()
      println("What do you want to make today? (Type the recipe number) ")
      var recipe: String = readLine()
      if (recipe.toLowerCase == "quit") {gameLoop = false}
      else {
        if(recipe forall Character.isDigit){
          println("Okay, that will take: ")
          recipeWillTake(recipe)
          println("Are you sure? Y/n")
          var answer = readLine()
          if(answer.toLowerCase()=="quit"){
            gameLoop = false
          }else{
            if(answer(0) == 'y') {
              if(checkIngr(recipe)) {
                subtractIngr(recipe)
                money += sell(recipe)
                println("Would you like to go to the store?")
                answer = readLine()
                if (answer.toLowerCase() == "quit") {
                  gameLoop = false
                } else {
                    if (answer(0) == 'y') {
                      val randFood = randy.nextInt(13) + 1
                      val quant = randy.nextInt(20) + 1
                      val divi = randy.nextInt(11) + 1
                      val price = (randFood * quant) / divi
                      val todaysFood = statement.executeQuery("select foodName from food where food.id=" + randFood + ';')
                      while (todaysFood.next()) {
                        println("Today the shop is selling " + quant + " " + todaysFood.getString(1) + " for $" + price + '.')
                      }
                      println("Will you purchase? Y/n")
                      answer = readLine()
                      if (answer.toLowerCase() == "quit") {
                        gameLoop = false
                      }
                      else {
                        if (answer(0) == 'y') {
                          if(money < price){
                            println("You don't have enough money!")
                          }else{
                            money = money - price
                            val stocksATM = statement.executeQuery("SELECT id from stock;")
                            var haveSome = false
                            while(stocksATM.next()){
                              if(stocksATM.getString(1).toInt==randFood){
                                haveSome = true
                              }
                            }
                            if(haveSome){
                              val newStock =  statement.executeUpdate("UPDATE stock SET quantity = quantity+"+quant+" WHERE stock.id = "+randFood+';')
                            }else{
                              val newStock = statement.executeUpdate("INSERT INTO stock(id, quantity) VALUES("+randFood+','+quant+");")
                            }
                          }
                        }
                      }
                    }
                }
              }else{println("You don't have the right ingredients!")}
            }
          }
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
  def checkIngr(recipe: String): Boolean= {
    var haveEnough = true
    val stockList = statement.executeQuery("SELECT * FROM stock;")
    var stocks = scala.collection.mutable.Map[String, String]()
    while (stockList.next()) {
      stocks += (stockList.getString(1) -> stockList.getString(2))
    }
    val recipeNeeds = statement.executeQuery("select * from recipe WHERE recipe.id=" + recipe.toInt + ";")
    while (recipeNeeds.next()) {
      for (x <- 3 to 11 by 2) {
        if(!(recipeNeeds.getString(x) == null)){
          if(stocks.apply(recipeNeeds.getString(x)).toInt < recipeNeeds.getString(x+1).toInt){
            haveEnough = false
          }
        }
      }
    }
    haveEnough
  }
  def subtractIngr(recipe: String): Unit={
    val stockList = statement.executeQuery("SELECT * FROM stock;")
    var stocks = scala.collection.mutable.Map[String, String]()
    while (stockList.next()) {
      stocks += (stockList.getString(1) -> stockList.getString(2))
    }
    val recipeNeeds = statement.executeQuery("select * from recipe WHERE recipe.id=" + recipe.toInt + ";")
    var ingrList = scala.collection.mutable.Map[String, String]()
    while (recipeNeeds.next()) {
      for (x <- 3 to 11 by 2) {
        if(!(recipeNeeds.getString(x) == null)){
          ingrList += (recipeNeeds.getString(x) -> recipeNeeds.getString(x+1))
        }
      }
    }
    for ((k,v) <- ingrList) {
      val changeIngr = statement.executeUpdate("UPDATE stock SET quantity=quantity-"+v+" WHERE stock.id ="+k+";")
    }
  }
  def sell(recipe: String): Int={
    val cash = randy.nextInt(100)
    val resultSet = statement.executeQuery("select recipeName from recipe where recipe.id ="+recipe.toInt+';')
    while (resultSet.next()){
      println("You made $"+cash+ " from selling your "+resultSet.getString(1)+'.')
    }
    return cash
  }
}
