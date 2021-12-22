package main

import java.awt.Canvas
import java.awt.Color

class Game(cols : Int, rows : Int, numberOfMines : Int) : Canvas(), Runnable
{
	private val gameWidth : Int
	private val gameHeight : Int
	
	private var running = false
	
	private var thread : Thread? = null
	private var handler : Handler = Handler(cols, rows, numberOfMines)
	
	@Synchronized
	fun start()
	{
		if(running) return
		
		thread = Thread(this)
		thread!!.start()
		
		running = true
	}
	
	@Synchronized
	private fun stop()
	{
		if(!running) return
		
		running = false
		
		try
		{
			thread!!.interrupt()
		}
		
		catch(e : Exception)
		{
			e.printStackTrace()
			exitProcess(0)
		}
	}
	
	override fun run()
	{
		this.requestFocus()
		
		while(running)
			render()
	}
	
	private fun render()
	{
		val bs = bufferStrategy
		
		if(bs == null)
		{
			this.createBufferStrategy(3)
			return
		}
		
		val g = bs.drawGraphics
		
		g.color = Color.darkGray
		g.fillRect(0, 0, gameWidth, gameHeight)
		
		handler.render(g)
		
		g.dispose()
		bs.show()
	}
	
	init
	{
		gameWidth = handler.pixelWidth + handler.xOffset*2
		gameHeight = handler.pixelHeight + handler.yOffset*3 + handler.statBarSize
		
		addMouseListener(MouseInput(handler))
		addMouseMotionListener(MouseInput(handler))
		
		setSize(gameWidth, gameHeight)
		GameScreen(gameWidth, gameHeight, this)
	}
}
