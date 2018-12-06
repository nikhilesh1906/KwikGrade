package views;

import helpers.FileManager;
import views.components.GradingSchemeGrid;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import models.Course;
import models.CourseCategory;
import models.GraduateStudent;
import models.OverallGrade;
import models.Student;
import models.SubCategory;
import models.UndergraduateStudent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddStudentFrame extends JDialog {
	private final JPanel contentPanel = new JPanel();
	private JTextField fNameField;
	private JTextField lNameField;
	private JTextField buIdField;
	private JTextField emailField;
	private JTextField mInitialField;
	private JLabel lNameLabel;
	private JLabel buIdLabel;
	private JLabel emailLabel;
	private JLabel statusLabel;
	private JLabel mInitialLabel;
	private JScrollPane gradingSchemeScrollPane;

	private Student newStudent;
	private OverallGrade studentOverallGrade;
	private OverallGrade overallGradeScheme;
	private GradingSchemeGrid gradingSchemeGrid;

	/**
	 * Create the dialog.
	 */
	public AddStudentFrame(Course managedCourse) {
		overallGradeScheme = managedCourse.getCourseUnderGradDefaultGradeScheme();

		setBounds(100, 100, 1000, 600);

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		GridBagConstraints frameConstraints = new GridBagConstraints();

		fNameField = new JTextField();
		fNameField.setBounds(53, 60, 130, 26);
		contentPanel.add(fNameField);
		fNameField.setColumns(10);

		lNameField = new JTextField();
		lNameField.setBounds(382, 60, 130, 26);
		lNameField.setColumns(10);
		contentPanel.add(lNameField);

		buIdField = new JTextField();
		buIdField.setBounds(53, 124, 130, 26);
		buIdField.setColumns(10);
		contentPanel.add(buIdField);

		emailField = new JTextField();
		emailField.setBounds(217, 124, 130, 26);
		emailField.setColumns(10);
		contentPanel.add(emailField);

		JLabel lblFirstName = new JLabel("First Name (Required)");
		lblFirstName.setBounds(53, 45, 130, 16);
		contentPanel.add(lblFirstName);

		lNameLabel = new JLabel("Last Name (Required)");
		lNameLabel.setBounds(382, 45, 130, 16);
		contentPanel.add(lNameLabel);

		buIdLabel = new JLabel("BU ID (Required)");
		buIdLabel.setBounds(53, 109, 130, 16);
		contentPanel.add(buIdLabel);

		emailLabel = new JLabel("Email (Required)");
		emailLabel.setBounds(217, 109, 130, 16);
		contentPanel.add(emailLabel);

		statusLabel = new JLabel("Status (Required)");
		statusLabel.setBounds(382, 109, 130, 16);
		contentPanel.add(statusLabel);

		mInitialField = new JTextField();
		mInitialField.setBounds(217, 60, 130, 26);
		mInitialField.setColumns(10);
		contentPanel.add(mInitialField);

		mInitialLabel = new JLabel("Middle Initial (Optional)");
		mInitialLabel.setBounds(217, 45, 130, 16);
		contentPanel.add(mInitialLabel);
		
		JComboBox studentStatusDropdown = new JComboBox();
		studentStatusDropdown.addItem("Undergraduate");
		studentStatusDropdown.addItem("Graduate");

		studentStatusDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (studentStatusDropdown.getSelectedItem().equals("Undergraduate")) {
					overallGradeScheme = managedCourse.getCourseUnderGradDefaultGradeScheme();
				}
				else {
					overallGradeScheme = managedCourse.getCourseGradDefaultGradeScheme();
				}
				// Rerenders a the grading scheme by removing/adding to the content panel.
				gradingSchemeGrid = new GradingSchemeGrid(overallGradeScheme);
				gradingSchemeGrid.configureGradingSchemeGrid(GradingSchemeGrid.GradingSchemeType.ADD_STUDENT);
				contentPanel.remove(gradingSchemeScrollPane);
				gradingSchemeScrollPane = gradingSchemeGrid.buildGradingSchemeGrid();
				contentPanel.add(gradingSchemeScrollPane);
				contentPanel.revalidate();
				contentPanel.repaint();
			}
		});

		studentStatusDropdown.setBounds(382, 127, 130, 26);
		contentPanel.add(studentStatusDropdown);

		// Add panel to frame
		frameConstraints.gridx = 0;
		frameConstraints.gridy = 1;
		frameConstraints.weighty = 1;

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton saveButton = new JButton("Save and Add");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create a new student from the TextFields.
				
				//pulls data into variables to make code easier to read/follow, and to allow for a check of required fields
				String fName = fNameField.getText();
				String middleInitial;
				String lName = lNameField.getText();
				String buId = buIdField.getText();
				String email = emailField.getText();
				String gradUndergradStatus = (String) studentStatusDropdown.getSelectedItem();
				
				if (fName.equals("")||lName.equals("")||buId.equals("")||email.equals("")||studentStatusDropdown.getSelectedIndex()==-1) {
					JOptionPane.showMessageDialog(null, "Please make sure all required fields are filled!");
					return;
				}
				
				//sets middle initial, if it is not specified
				if (mInitialField.getText() == null) {
					middleInitial = "";
				}
				else {
					middleInitial = mInitialField.getText();
				}
								
				// Creates undergraduate or graduate students, and creates copies of default grading schemes.
				if (gradUndergradStatus.equals("Undergraduate")) {
					studentOverallGrade = gradingSchemeGrid.getOverallGradeFromFields();
					newStudent = new UndergraduateStudent(fName, middleInitial, lName, buId, email, gradUndergradStatus, studentOverallGrade);
				}
				else {
					studentOverallGrade = gradingSchemeGrid.getOverallGradeFromFields();
					newStudent = new GraduateStudent(fName, middleInitial, lName, buId, email, gradUndergradStatus, studentOverallGrade);
				}
				// Add the student to the course.
				managedCourse.addActiveStudents(newStudent);

				// Save the changes.
				FileManager.saveFile(MainDashboard.getKwikGrade().getActiveCourses(), MainDashboard.getActiveSaveFileName());
				FileManager.saveFile(MainDashboard.getKwikGrade().getClosedCourses(), MainDashboard.getClosedSaveFileName());
				dispose();
			}
		});
		buttonPane.add(saveButton);
		getRootPane().setDefaultButton(saveButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		// ======================================
		// Build Grading Scheme Grid Layout
		// ======================================
		gradingSchemeGrid = new GradingSchemeGrid(overallGradeScheme);
		gradingSchemeGrid.configureGradingSchemeGrid(GradingSchemeGrid.GradingSchemeType.ADD_STUDENT);
		gradingSchemeScrollPane = gradingSchemeGrid.buildGradingSchemeGrid();
		contentPanel.add(gradingSchemeScrollPane);

	}
}
