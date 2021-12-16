package main

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextField

class MenuScreen : JFrame("select difficulty")
{
	private val bgWidth = 250
	private val bgHeight = 360
	private val frameSize = Dimension(bgWidth + 7, bgHeight + 20)
	
	private var flag = false
	
	init
	{
		val panel = JPanel()
		panel.minimumSize = frameSize
		panel.preferredSize = frameSize
		panel.maximumSize = frameSize
		
		panel.background = Color.darkGray
		
		panel.layout = null
		panel.add(Selection(3, 4, 0))
		panel.add(Selection(3, 8 + bgHeight/4, 1))
		panel.add(Selection(3, 12 + bgHeight/2, 2))
		panel.add(Selection(3, 16 + bgHeight*3/4, 3))
		
		add(panel)
		pack()
		
		isResizable = false
		setLocationRelativeTo(null)
		defaultCloseOperation = EXIT_ON_CLOSE
		isVisible = true
	}
	
	inner class Selection(x : Int, y : Int, val index : Int) : JPanel()
	{
		var col : Color = Color.lightGray
		
		init
		{
			setBounds(x, y, bgWidth, bgHeight/4)
			
			addMouseListener(when(index) {
				3 -> CustomSelector()
				else -> Selector()
			})
		}
		
		override fun paint(g : Graphics)
		{
			g.color = col.darker()
			g.fillRect(0, 0, bgWidth, bgHeight/4)
			
			g.color = col
			g.fillRect(2, 2, bgWidth - 4, bgHeight/4 - 4)
			
			g.color = Color.black
			g.font = Font(null, Font.PLAIN, 20)
			
			when(index)
			{
				0 -> {
					g.drawString("beginner", bgWidth / 2 - 40, bgHeight / 16)
					g.drawString("9 * 9  /  10", bgWidth / 4 + 15, bgHeight*3 / 16)
				}
				
				1 -> {
					g.drawString("intermediate", bgWidth / 2 - 55, bgHeight / 16)
					g.drawString("16 * 16  /  40", bgWidth / 4 + 7, bgHeight*3 / 16)
				}
				
				2 -> {
					g.drawString("expert", bgWidth / 2 - 28, bgHeight / 16)
					g.drawString("30 * 16  /  99", bgWidth / 4 + 7, bgHeight*3 / 16)
				}
				
				3 -> {
					g.drawString("custom", bgWidth / 2 - 32, bgHeight / 16)
					g.drawString("__ * __  /  __", bgWidth / 4 + 10, bgHeight*3 / 16)
				}
				
				else -> return
			}
		}
		
		open inner class Selector : MouseAdapter()
		{
			override fun mousePressed(e : MouseEvent?)
			{
				col = Color.green.brighter()
				repaint()
			}
			
			override fun mouseReleased(e : MouseEvent?)
			{
				col = Color.lightGray
				repaint()
				
				when(index)
				{
					0 -> Game(9, 9, 10)
					1 -> Game(16, 16, 40)
					2 -> Game(30, 16, 99)
					else -> return
				}
			}
		}
		
		inner class CustomSelector : Selector()
		{
			private val colsInput = JTextField("0")
			private val rowsInput = JTextField("0")
			private val minesInput = JTextField("0")
			
			override fun mousePressed(e : MouseEvent?)
			{
				col = if(!flag) Color.gray else Color.green.brighter()
				repaint()
			}
			
			override fun mouseReleased(e : MouseEvent?)
			{
				if(!flag)
				{
					colsInput.setBounds(bgWidth / 4 + 8, bgHeight / 8, 26, 25)
					rowsInput.setBounds(bgWidth / 2 - 13, bgHeight / 8, 26, 25)
					minesInput.setBounds(bgWidth * 3 / 4 - 25, bgHeight / 8, 26, 25)
					
					layout = null
					add(colsInput)
					add(rowsInput)
					add(minesInput)
				}
				
				else
				{
					val cols = colsInput.text.toIntOrNull()
					val rows = rowsInput.text.toIntOrNull()
					val mines = minesInput.text.toIntOrNull()
					
					colsInput.optimize(cols, 6, 75)
					rowsInput.optimize(rows, 6, 35)
					minesInput.optimize(mines, 1,
										if(cols != null && rows != null)
									  minOf(cols*rows-2, 999)
								  else 1)
					
					if(cols != null && rows != null && mines != null
					   && cols in 6..75 && rows in 6..35 && mines in 1..rows*cols - 2)
						Game(cols, rows, mines)
					
					col = Color.gray
					repaint()
				}
				
				colsInput.refresh()
				rowsInput.refresh()
				minesInput.refresh()
				
				flag = true
			}
			
			private fun JTextField.optimize(x : Int?, min : Int, max : Int)
			{
				if(x == null || x < min || x > max) text = "0"
			}
			
			private fun JTextField.refresh()
			{
				val org = text
				text = org
			}
		}
	}
}