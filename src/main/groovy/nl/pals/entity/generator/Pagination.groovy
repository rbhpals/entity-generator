package nl.pals.entity.generator

import groovy.json.JsonOutput
import javax.swing.*
import javax.swing.table.DefaultTableModel

import com.fasterxml.jackson.core.io.DataOutputAsStream

import java.awt.*
import java.awt.event.*

class Pagination {
	private static final int DEFAULT_PAGE_SIZE = 5
	private static int[] PAGE_SIZES = [5, 10, 15, 20]
	private int PAGE_SIZES_MAX
	private static final String ALL_OPTION = "ALL"
	private java.util.List<String> data = new ArrayList<>()
	private int currentPage = 0
	private int pageSize = DEFAULT_PAGE_SIZE
	private JTable table
	private JLabel pageLabel
	// Content window reference
	private JFrame contentFrame = null

	Pagination(rows) {

		// Bepaal max aantal regels per pagina
		PAGE_SIZES_MAX = rows.size() // PAGE_SIZES_MAX is gelijk aan het aantal rijen
		rows.each { element ->
			data.add(element)
		}

		def frame = new JFrame("Generated Entities")
		frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
		frame.setLayout(new BorderLayout())
		frame.setSize(800, 400)

		def tPanel = new JPanel()
		JScrollPane scrollPane = new JScrollPane()
		table = new JTable(getPageData(), ["Content"] as String[])
		table.setFillsViewportHeight(true)
		addEventToTable(table);
		scrollPane.setViewportView(table)
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)

		tPanel.add(scrollPane)
		frame.add(tPanel, BorderLayout.CENTER)

		def pPanel = new JPanel()
		def flowLayout = new FlowLayout(FlowLayout.LEFT)
		JButton firstButton = new JButton("<<")
		JButton previousButton = new JButton("<")
		JButton nextButton = new JButton(">")
		JButton lastButton = new JButton(">>")
		pageLabel = new JLabel(getPageLabelText())
		JComboBox<String> pageSizeCombo = new JComboBox<>()

		// Add page sizes to the combo box
		PAGE_SIZES.each { pageSizeCombo.addItem(it.toString()) }
		pageSizeCombo.addItem(ALL_OPTION) // Voeg "ALL" option toe

		firstButton.addActionListener { goToPage(0) }
		previousButton.addActionListener  { goToPage(currentPage - 1) }
		nextButton.addActionListener  { goToPage(currentPage + 1) }
		lastButton.addActionListener  { goToPage(getLastPageIndex()) }

		pageSizeCombo.addActionListener {
			if (pageSizeCombo.selectedItem == ALL_OPTION) {
				pageSize = PAGE_SIZES_MAX // Indien "ALL" neem dan PAGE_SIZES_MAX over die eerder gezet is
			} else {
				pageSize = Integer.parseInt(pageSizeCombo.selectedItem)
			}
			goToPage(0) // Reset to first page when page size changes
		}


		pPanel.add(firstButton)
		pPanel.add(previousButton)
		pPanel.add(pageLabel)
		pPanel.add(pageSizeCombo)
		pPanel.add(nextButton)
		pPanel.add(lastButton)
		frame.add(pPanel, BorderLayout.SOUTH)
		frame.setVisible(true)
	}

	private void goToPage(int page) {
		if (page < 0 || page > getLastPageIndex()) return
			currentPage = page
		table.setModel(new DefaultTableModel(getPageData(), ["Column 1"] as String[]))
		pageLabel.text = getPageLabelText()
	}

	private String getPageLabelText() {
		return "Page ${currentPage + 1} of ${getLastPageIndex() + 1}"
	}

	private int getLastPageIndex() {
		return (data.size().intdiv(pageSize)).intdiv(1) - (data.size() % pageSize == 0 ? 1 : 0)
	}

	private Object[][] getPageData() {
		int start = currentPage * pageSize
		int end = Math.min(start + pageSize, data.size())
		// 2 dim array teruggeven, waarbij 1e dimensie de rij data is en de 2e dimensie de kolom waar de data in komt
		return data.subList(start, end).collect { [it] } as Object[][]
	}

	private void addEventToTable(table) {
		// Row click listener
		table.addMouseListener(new MouseAdapter() {
					@Override
					void mouseClicked(MouseEvent e) {
						int row = table.getSelectedRow()
						if (row != -1) {
							//String content = table.getValueAt(row, 1).toString()
							// content alle tekens tot aan { verwijderen bijv Result voor run 1: {"Contract.. wordt {"Contract....
							String content = table.getValueAt(row, 0).toString().replaceFirst(".*?\\{", "{")
							content = JsonOutput.prettyPrint(content)
							if (contentFrame == null || !contentFrame.isDisplayable()) {
								contentFrame = showContentWindow(content)
							} else {
								updateContentWindow(contentFrame, content)
							}
							//showContentWindow(JsonOutput.prettyPrint(content))
						}
					}
				})

		// Key listener for arrow down key
		table.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
							int row = table.getSelectedRow()
							if (row != -1) {
								//String content = table.getValueAt(row, 1).toString()
								// content alle tekens tot aan { verwijderen bijv Result voor run 1: {"Contract.. wordt {"Contract....
								String content = table.getValueAt(row, 0).toString().replaceFirst(".*?\\{", "{")
								content = JsonOutput.prettyPrint(content)
								if (contentFrame == null || !contentFrame.isDisplayable()) {
									contentFrame = showContentWindow(content)
								} else {
									updateContentWindow(contentFrame, content)
								}
							}
						}
					}
				})
	}

	// Function to create and show a new window with the content of the selected row
	private JFrame showContentWindow(String content) {
		def contentFrame = new JFrame("Json data structure")
		contentFrame.setLayout(new BorderLayout())
		contentFrame.setSize(300, 200)

		def textArea = new JTextArea(content)
		textArea.setEditable(false)
		contentFrame.add(new JScrollPane(textArea), BorderLayout.CENTER)

		def closeButton = new JButton("Close")
		closeButton.addActionListener { contentFrame.dispose() }
		contentFrame.add(closeButton, BorderLayout.SOUTH)

		contentFrame.setVisible(true)
		return contentFrame
	}

	private void updateContentWindow(JFrame contentFrame, String content) {
		// Update the content of the existing window
		JTextArea textArea = (JTextArea) ((JScrollPane) contentFrame.getContentPane().getComponent(0)).getViewport().getView()
		textArea.setText(content)
	}
}



