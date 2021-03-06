package views.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import helpers.KwikGradeUIManager;
import helpers.ModelGenerators;
import helpers.StudentTextImport;
import models.Course;
import models.OverallGrade;
import models.Student;
import views.frames.MainDashboardFrame;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JComboBox;

public class CreateFromExistingDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField courseNumField;
	private JTextField courseTermField;
	private JTextField courseTitleField;
	private JTextField studentFilepathField;

	private String courseNum;
	private String courseTerm;
	private String courseTitle;
	private String filePath;
	private int cloneCourseIndex;
	private boolean hasCreatedNewCourse = false;

	private ArrayList<Student> importedStudentList = new ArrayList<>();
	private OverallGrade clonedUGGradingScheme;
	private OverallGrade clonedGradGradingScheme;

	/**
	 * Create the dialog to create a course from an existing course.
	 */
	public CreateFromExistingDialog() {
		KwikGradeUIManager.setUpUI(this, contentPanel, 595, 748);

		// ============================================
		// Enter Course Details
		// ============================================
		courseNumField = new JTextField();
		courseNumField.setBounds(222, 29, 343, 36);
		contentPanel.add(courseNumField);
		courseNumField.setColumns(10);
		
		courseTermField = new JTextField();
		courseTermField.setColumns(10);
		courseTermField.setBounds(222, 78, 343, 36);
		contentPanel.add(courseTermField);
		
		courseTitleField = new JTextField();
		courseTitleField.setColumns(10);
		courseTitleField.setBounds(222, 127, 343, 36);
		contentPanel.add(courseTitleField);

		JLabel courseNumberLabel = new JLabel("Course Number (required)");
		courseNumberLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		courseNumberLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		courseNumberLabel.setBounds(12, 33, 187, 26);
		contentPanel.add(courseNumberLabel);
		
		JLabel courseTermLabel = new JLabel("Course Term (required)");
		courseTermLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		courseTermLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		courseTermLabel.setBounds(12, 82, 187, 26);
		contentPanel.add(courseTermLabel);
		
		JLabel courseTitleLabel = new JLabel("Course Title (required)");
		courseTitleLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		courseTitleLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		courseTitleLabel.setBounds(12, 131, 187, 26);
		contentPanel.add(courseTitleLabel);
		
		// Get JLabel to display across multiple lines using HTML.
		JLabel importNowLabel = new JLabel("<html>Add students by importing now.<br/>(Or add them manually later)</html>");
		importNowLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		importNowLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		importNowLabel.setBounds(167, 533, 243, 36);
		contentPanel.add(importNowLabel);

		studentFilepathField = new JTextField();
		studentFilepathField.setColumns(10);
		studentFilepathField.setBounds(79, 617, 405, 36);
		studentFilepathField.setEditable(false);
		contentPanel.add(studentFilepathField);

		JLabel courseCloneSelectLabel = new JLabel("Select a Course to Clone");
		courseCloneSelectLabel.setFont(new Font("Tahoma", Font.PLAIN, 16));
		courseCloneSelectLabel.setBounds(63, 180, 217, 25);
		contentPanel.add(courseCloneSelectLabel);

		// ============================================
		// Importing students by file browser
		// ============================================
		contentPanel.add(new JSeparator());
		JButton browseButton = new JButton("Browse File Path of Student Text File...");
		browseButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		browseButton.setBounds(79, 573, 405, 36);
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

				// For File
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);

				int userChoice = fileChooser.showOpenDialog(null);
				if (userChoice == JFileChooser.APPROVE_OPTION) {
					String studentTextFilePath = fileChooser.getSelectedFile().toString();
					studentFilepathField.setText(studentTextFilePath);
				}
			}
		});
		contentPanel.add(browseButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(34, 216, 499, 304);
		contentPanel.add(scrollPane);

		JList cloneCourseList = new JList();
		scrollPane.setViewportView(cloneCourseList);
		cloneCourseList.setFont(new Font("Tahoma", Font.PLAIN, 18));
		cloneCourseList.setModel(ModelGenerators.generateCourseTableModel(MainDashboardFrame.getKwikGrade().getActiveCourses()));
		JComboBox openClosedCourses = new JComboBox();
		openClosedCourses.addItem("Active Courses");
		openClosedCourses.addItem("Closed Courses");
		openClosedCourses.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (openClosedCourses.getSelectedItem().equals("Active Courses")) {
					cloneCourseList.setModel(ModelGenerators.generateCourseTableModel(MainDashboardFrame.getKwikGrade().getActiveCourses()));
				}
				else {
					cloneCourseList.setModel(ModelGenerators.generateCourseTableModel(MainDashboardFrame.getKwikGrade().getClosedCourses()));
				}
			}
		});
		openClosedCourses.setBounds(290, 181, 168, 26);

		contentPanel.add(openClosedCourses);

		// Set action buttons
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				courseNum = courseNumField.getText();
				courseTerm = courseTermField.getText();
				courseTitle = courseTitleField.getText();
				filePath = studentFilepathField.getText();
				cloneCourseIndex = cloneCourseList.getSelectedIndex();

				int cloneIndex = cloneCourseList.getSelectedIndex();
				if(cloneIndex == -1) {
					JOptionPane.showMessageDialog(null, "You have not selected a course!");
					return;
				}

				if (openClosedCourses.getSelectedItem().equals("Active Courses")) {
					clonedUGGradingScheme = clonedUGGradingScheme.copyOverallGrade(getUGGradeScheme(MainDashboardFrame.getKwikGrade().getActiveCourses(), cloneIndex));
					clonedGradGradingScheme = clonedGradGradingScheme.copyOverallGrade(getGradGradeScheme(MainDashboardFrame.getKwikGrade().getActiveCourses(), cloneIndex));
				}
				else {
					clonedUGGradingScheme = clonedUGGradingScheme.copyOverallGrade(getUGGradeScheme(MainDashboardFrame.getKwikGrade().getClosedCourses(), (cloneIndex)));
					clonedGradGradingScheme = clonedGradGradingScheme.copyOverallGrade(getGradGradeScheme(MainDashboardFrame.getKwikGrade().getClosedCourses(), (cloneIndex)));
				}

				if(courseNum.equals("") || courseTerm.equals("") || courseTitle.equals("")) {
					JOptionPane.showMessageDialog(null, "Must enter in required information!");
					return;
				}

				if(!filePath.equals("")) {
					importedStudentList = StudentTextImport.addImportedStudents(filePath, clonedUGGradingScheme, clonedGradGradingScheme);
				}

				hasCreatedNewCourse = true;
				
				dispose();
			}
		});
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPane.add(cancelButton);
	}

	//=============================
	// Getters
	//=============================
	public OverallGrade getUGGradeScheme(ArrayList<Course> courseList, int selectedCourse) {
		OverallGrade gradeScheme = courseList.get(selectedCourse).getCourseUnderGradDefaultGradeScheme();
		return gradeScheme;
	}

	public OverallGrade getGradGradeScheme(ArrayList<Course> courseList, int selectedCourse) {
		OverallGrade gradeScheme = courseList.get(selectedCourse).getCourseGradDefaultGradeScheme();
		return gradeScheme;
	}
	public String getCourseNum() {
		return courseNum;
	}
	public String getCourseTerm() {
		return courseTerm;
	}
	public String getCourseTitle() {
		return courseTitle;
	}
	public ArrayList<Student> getImportedStudentsList() {
		return this.importedStudentList;
	}
	public String getFilePath() {
		return filePath;
	}
	public boolean getHasCreatedNewCourse() {
		return this.hasCreatedNewCourse;
	}
	public int getCloneCourseIndex() {
		return this.cloneCourseIndex;
	}
	public OverallGrade getUGOverallGrade() {
		return clonedUGGradingScheme;
	}
	public OverallGrade getGradOverallGrade() {
		return clonedGradGradingScheme;
	}
	
	//=============================
	// Setters
	//=============================
	
	public void setCourseNum(String courseNum) {
		this.courseNum = courseNum;
	}
	public void setCourseTerm(String courseTerm) {
		this.courseTerm = courseTerm;
	}
	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
