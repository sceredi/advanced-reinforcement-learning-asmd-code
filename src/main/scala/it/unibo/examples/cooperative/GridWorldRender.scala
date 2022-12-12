package it.unibo.examples.cooperative

import it.unibo.model.core.abstractions.Scheduler
import it.unibo.view.Render
import javax.swing.WindowConstants
import java.awt.{Color, GridLayout}
import javax.swing.{JButton, JFrame, JLabel, SwingUtilities}
class GridWorldRender(using scheduler: Scheduler)(
    bound: Int,
    await: Int,
    private var each: Int
) extends Render[BoundedWorldEnvironment.State] {
  private val frame = JFrame(s"Grid world render -- episode: ${scheduler.episode}")
  private val labels = (0 to bound).flatMap(i => (0 to bound).map(j => (i, j) -> new JLabel(s"$i, $j")))
  private val labelsMap = labels.toMap
  labelsMap.values.foreach(_.setOpaque(true))
  frame.getContentPane.setLayout(new GridLayout(bound + 1, bound + 1))
  labels.foreach { case (_, label) => frame.getContentPane.add(label) }
  frame.setSize(800, 600)
  frame.setVisible(true)
  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  override def render(state: BoundedWorldEnvironment.State): Unit = if (scheduler.episode % each == 0)
    SwingUtilities.invokeAndWait { () =>
      frame.setTitle(s"Grid world render -- episode: ${scheduler.episode}")
      labelsMap.values.foreach(_.setBackground(Color.GRAY))
      state.foreach { case (x, y) =>
        val label = labelsMap(x, y)
        label.setBackground(Color.CYAN)
      }
    }
    Thread.sleep(await)

  def renderEach(value: Int): Unit =
    each = value
}
