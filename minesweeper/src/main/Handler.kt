package main

import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import kotlin.properties.Delegates
import kotlin.random.Random

class Handler(val cols : Int, val rows : Int, val numberOfMines : Int)
{
	val xOffset = 3
	val yOffset = 4
	
	val statBarSize = 40
	val tileSize = 20
	
	val pixelWidth = tileSize*cols
	val pixelHeight = tileSize*rows
	
	var flagCount = 0
	var closedTiles = cols*rows
	
	var statBarCol : Color = Color.lightGray
	
	var gameOver by Delegates.observable(false) {
		_, _, new -> if(new) statBarCol = Color.red
	}
	
	var win by Delegates.observable(false) {
		_, _, new -> if(new) statBarCol = Color.green
	}
	
	var started = false
	
	private var startedTime = 0L
	private var timer = 0L
	
	var map = Array(cols * rows) {
		Tile(it % cols, it / cols)
	}
	
	fun restoreTiles() =
		map.forEach {
			it.isExposed = false
			it.isFlagged = false
			it.isSeed = false
			it.hasMine = false
			it.isHighlighted = false
			it.isTrigger = false
			it.nearbyMines = 0
		}
	
	fun placeMines()
	{
		var mineCount = 0
		
		while(mineCount < numberOfMines)
		{
			val temp = map[Random.nextInt(cols*rows)]
			
			if(!temp.isSeed && !temp.hasMine)
			{
				temp.hasMine = true
				mineCount++
			}
		}
		
		map.forEach {
				temp -> temp.getNeighbors().forEach { if(it != null && it.hasMine) temp.nearbyMines++ }
		}
	}
	
	fun render(g : Graphics)
	{
		if(!started)
			startedTime = System.currentTimeMillis()
		
		if(!win && !gameOver)
			timer = (System.currentTimeMillis() - startedTime)/1000
		
		map.forEach { it.render(g) }
		renderStatBar(g)
	}
	
	private fun renderStatBar(g : Graphics)
	{
		g.color = statBarCol
		g.fillRect(xOffset, yOffset, pixelWidth, statBarSize)
		
		g.color = statBarCol.darker()
		g.fillRect(xOffset+2, yOffset+2, pixelWidth-4, statBarSize-4)
		
		g.color = Color.white
		g.font = Font(null, Font.PLAIN, 18)
		g.drawString("Timer : $timer", xOffset + 4, yOffset + 16)
		g.drawString("Mines : " + if(win) "0" else (numberOfMines - flagCount),
					 xOffset + 4, yOffset + 36)
	}
	
	inner class Tile(private val x : Int, private val y : Int)
	{
		var isExposed by Delegates.observable(false) {
			_, old, new -> if(old != new) closedTiles--
		}
		
		var isFlagged by Delegates.observable(false) {
			_, _, newVal -> if(newVal) flagCount++ else flagCount--
		}
		
		var isHighlighted = false
		var hasMine = false
		var isTrigger = false
		var isSeed = false
		var nearbyMines = 0
		
		private val leftEnd = x * tileSize + xOffset
		private val topEnd = y * tileSize + yOffset*2 + statBarSize
		
		fun getNeighbors() = Array(9) {
			val xCord = x - 1 + (it % 3)
			val yCord = y - 1 + (it / 3)
			
			if(xCord in 0 until cols && yCord in 0 until rows)
				map[xCord + yCord * cols]
			
			else null
		}
		
		fun render(g : Graphics)
		{
			g.color = Color.WHITE
			g.fillRect(leftEnd, topEnd, tileSize, tileSize)
			
			g.color = when {
				gameOver && hasMine && !isFlagged -> if(isTrigger) Color.black else Color.red
				(isHighlighted || isExposed) -> Color.lightGray
				win && hasMine -> Color.green
				isFlagged -> if(gameOver && !hasMine) Color.ORANGE.darker() else Color.yellow
				
				else -> Color.gray
			}
			
			g.fillRect(leftEnd, topEnd, tileSize - 1, tileSize - 1)
			
			if(isExposed && !hasMine && nearbyMines > 0)
			{
				g.font = Font(null, Font.BOLD, tileSize * 9 / 10)
				
				g.color = when(nearbyMines) {
					1 -> Color.blue
					2 -> Color.green.darker()
					3 -> Color.red
					4 -> Color.blue.darker()
					5 -> Color.red.darker()
					6 -> Color.cyan.darker().darker()
					7 -> Color.black
					else -> Color.gray
				}
				
				g.drawString(nearbyMines.toString(), leftEnd + 4, topEnd + 16)
			}
		}
		
		fun isComplete() =
			nearbyMines == getNeighbors().count { it != null && it.isFlagged }
		
		fun checkForNeighbors()
		{
			getNeighbors()
				.filter { it != null && it != this && !it.isFlagged && !it.isExposed}
				.forEach {
					it!!.isExposed = true
					
					if(it.hasMine)
					{
						it.isTrigger = true
						gameOver = true
						started = false
					}
					
					if(it.nearbyMines == 0)
						it.checkForNeighbors()
			}
		}
	}
}