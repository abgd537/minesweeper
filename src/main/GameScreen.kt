package main

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame

class GameScreen(width : Int, height : Int, private val game : Game) : JFrame()
{
	init
	{
		setSize(width, height)
		isResizable = false
		setLocationRelativeTo(null)
		addWindowListener(GameCloser())
		defaultCloseOperation = DO_NOTHING_ON_CLOSE
		
		add(game)
		pack()
		
		isVisible = true
		game.start()
	}
	
	inner class GameCloser : WindowAdapter()
	{
		override fun windowClosing(e : WindowEvent?)
		{
			game.stop()
			dispose()
		}
	}
}