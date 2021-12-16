package main

import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

class MouseInput(private val handler : Handler) : MouseAdapter()
{
	private var triedToRestart = false
	
	private fun getTile(e : MouseEvent) : Handler.Tile?
	{
		val relX = e.x - handler.xOffset
		val relY = e.y - handler.yOffset*2 - handler.statBarSize
		
		return if((relX in 0 until handler.pixelWidth) && (relY in 0 until handler.pixelHeight))
			handler.map[relX / handler.tileSize + (relY / handler.tileSize) * handler.cols]
		
		else null
	}
	
	override fun mousePressed(e : MouseEvent)
	{
		if(e.x - handler.xOffset in (0..handler.pixelWidth) && e.y - handler.yOffset in (0.. handler.statBarSize))
		{
			handler.statBarCol = handler.statBarCol.darker()
			triedToRestart = true
		}
		
		else
			highlight(e)
	}
	
	override fun mouseDragged(e : MouseEvent)
	{
		val relX = e.x - handler.xOffset
		val relY = e.y - handler.yOffset*2 - handler.statBarSize
		
		if((relX in 0 until handler.pixelWidth) && (relY in 0 until handler.pixelHeight))
			highlight(e)
		
		else
			handler.map.filter { it.isHighlighted }.forEach { it.isHighlighted = false }
	}
	
	private fun highlight(e : MouseEvent)
	{
		if(!handler.gameOver && !handler.win && SwingUtilities.isLeftMouseButton(e))
		{
			val tile = getTile(e)
			
			if(tile != null)
			{
				if(!tile.isExposed && !tile.isFlagged)
					tile.isHighlighted = true
				
				tile.getNeighbors()
					.filter { it != null && it != tile && it.isHighlighted }
					.forEach {
						it?.isHighlighted = false
					}
			}
		}
	}
	
	override fun mouseReleased(e : MouseEvent)
	{
		if(triedToRestart)
		{
			triedToRestart = false
			
			handler.statBarCol = if(handler.win) Color.green else if(handler.gameOver) Color.red else Color.lightGray
			
			if(e.x - handler.xOffset in (0..handler.pixelWidth) && e.y - handler.yOffset in (0.. handler.statBarSize))
			{
				handler.gameOver = false
				handler.win = false
				handler.started = false
				handler.statBarCol = Color.lightGray
				
				handler.restoreTiles()
				
				handler.flagCount = 0
				handler.closedTiles = handler.cols * handler.rows
			}
		}
		
		if(!handler.gameOver && !handler.win &&
		   (e.x - handler.xOffset !in (0..handler.pixelWidth) || e.y - handler.yOffset !in (0.. handler.statBarSize)))
		{
			val tile = getTile(e)
			
			if(tile != null)
			{
				if(!handler.started)
				{
					handler.started = true
					tile.isSeed = true
					handler.placeMines()
				}
				
				if(e.button == 3 && !tile.isExposed
				   && (tile.isFlagged || (!tile.isFlagged && handler.flagCount < handler.numberOfMines)))
						tile.isFlagged = !tile.isFlagged
				
				else if(e.button == 1 && !tile.isFlagged)
				{
					if(tile.hasMine)
					{
						tile.isTrigger = true
						handler.gameOver = true
					}
					
					else
					{
						if(!tile.isExposed)
							tile.isExposed = true
						
						if(!tile.isFlagged && tile.isComplete())
							tile.checkForNeighbors()
						
						if(handler.closedTiles == handler.numberOfMines)
							handler.win = true
					}
				}
			}
		}
	}
}