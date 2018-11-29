package views.components;

import models.CourseCategory;
import models.OverallGrade;
import models.SubCategory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GradingSchemeGrid {
    public enum GradingSchemeType {
        ADD_STUDENT, MANAGE_STUDENT, MANAGE_CATEGORIES;
    }

    private static int GRADING_SCHEME_WIDTH = 900;
    private static int GRADING_SCHEME_HEIGHT = 300;
    private static Color LIGHT_GRAY_COLOR = new Color(0xF0F0F0);
    private static Color DARK_GRAY_COLOR = new Color(0xE0E0E0);
    private static Color BORDER_COLOR = new Color(0xD1D0D1);
    private static Color LIGHT_GREEN_COLOR = new Color(0x97FFBF);
    private static Color LIGHT_BLUE_COLOR = new Color(0x0C97F5);


    private static int GRADING_SCHEME_COL_OFFSET = 2;

    // First two columns of the grading scheme grid are just header/titles.
    private static int gradingSchemeRowCount;
    private static String[] firstColumnText;
    private static String[] secondColumnText;

    private OverallGrade initialGradeScheme, modifiedGradeScheme;
    private GradingSchemeType gradingSchemeType;
    private List<List<JPanel>> schemeGrid = new ArrayList<List<JPanel>>();
    private HashMap<JPanel, CourseCategory> courseCategoryColMapping = new HashMap<>();
    private HashMap<JPanel, SubCategory> subCategoryColMapping = new HashMap<>();
    JPanel parentPanel = new JPanel();

    public GradingSchemeGrid(OverallGrade modifiedGradeScheme) {
        this.initialGradeScheme = modifiedGradeScheme;
        this.modifiedGradeScheme = modifiedGradeScheme;
    }

    public void configureGradingSchemeGrid(GradingSchemeType gradingSchemeType) {
        this.gradingSchemeType = gradingSchemeType;
        String finalGrade = Double.toString(this.modifiedGradeScheme.getOverallGrade());

        switch(gradingSchemeType) {
            case ADD_STUDENT:
                gradingSchemeRowCount = 5;
                // TODO: this Points Gained may need to dynamically change based on the switch we will add
                firstColumnText = new String[]{"", "", "Final Raw Score", "Points Gained on Item", "Total Points on Item"};
                secondColumnText = new String[]{"Final Grade", "", finalGrade, "", ""};
                break;
            case MANAGE_STUDENT:
                gradingSchemeRowCount = 5;
                firstColumnText = new String[]{"", "", "Final Raw Score", "Points Gained on Item", "Total Points on Item"};
                secondColumnText = new String[]{"Final Grade", "", finalGrade, "", ""};
                break;
            case MANAGE_CATEGORIES:
                gradingSchemeRowCount = 2;
                firstColumnText = new String[]{"", ""};
                secondColumnText = new String[]{"Final Grade", ""};
                break;
            default:
                break;
        }

        for(int i = 0; i < gradingSchemeRowCount; i++) {
            schemeGrid.add(new ArrayList<JPanel>());
        }
    }

    /**
     * Generates the grading scheme table, which is built using GridLayout.
     *
     * We build this using a nested structure as follows:
     * - JScrollPane that we return
     * 	- Parent JPanel that uses a GridLayout and holds...
     * 		- 2D ArrayList of JPanels, where each JPanel represents a "cell" in the table
     * 			- Each array cell can then contain JLabels, JLabels + JTextField, etc
     *
     * @return JScrollPane containing the entire grading scheme table
     */
    public JScrollPane buildGradingSchemeGrid() {
        parentPanel.setBackground(Color.WHITE);

        // Append initial header/title columns.
        appendTextColumn(firstColumnText);
        appendTextColumn(secondColumnText);

        // Define GridLayout.
        parentPanel.setLayout(new GridLayout(schemeGrid.size(), schemeGrid.get(0).size()));
        parentPanel.setBounds(75, 200, GRADING_SCHEME_WIDTH, GRADING_SCHEME_HEIGHT);

        // The first two columns are static with text. When we build the JPanels in the columns, they need to be offset by these initial columns of static text.
        for(int categoryIndex = 0; categoryIndex < modifiedGradeScheme.getCourseCategoryList().size(); categoryIndex++) {
            CourseCategory currCategory = modifiedGradeScheme.getCourseCategoryList().get(categoryIndex);

            appendCategoryColumn(currCategory);

            // TODO: delete this once we're able to have SubCategories and test this
            if(this.gradingSchemeType == GradingSchemeType.MANAGE_STUDENT) {
                SubCategory tmpSubCategory = new SubCategory(currCategory.getName() + "1", 0.4, 40.0, 50.0);
                currCategory.addSubCategory(tmpSubCategory);
                tmpSubCategory = new SubCategory(currCategory.getName() + "2", 0.5, 40.0, 50.0);
                currCategory.addSubCategory(tmpSubCategory);
            } else {
                SubCategory tmpSubCategory = new SubCategory(currCategory.getName() + "1", 0.4, 0.0, 50.0);
                currCategory.addSubCategory(tmpSubCategory);
                tmpSubCategory = new SubCategory(currCategory.getName() + "2", 0.5, 0.0, 50.0);
                currCategory.addSubCategory(tmpSubCategory);
            }


            for (int subCategoryIndex = 0; subCategoryIndex < currCategory.getSubCategoryList().size(); subCategoryIndex++) {
                SubCategory currSubCategory = currCategory.getSubCategoryList().get(subCategoryIndex);

                appendSubCategoryColumn(currSubCategory);
            }
        }

        // Add everything we just built into a JScrollPane.
        JScrollPane scrollPane = new JScrollPane(parentPanel);
        scrollPane.setBounds(75, 200, GRADING_SCHEME_WIDTH, GRADING_SCHEME_HEIGHT);

        return scrollPane;
    }

    /**
     * Given a SubCategory, generates a new column with SubCategory information.
     * Based on the grading scheme type we are serving this page from, we will add panels accordingly.
     * i.e. Add Student Page won't show the Points Gained/Total Points row but Manage Student Page will.
     *
     * @param currSubCategory
     */
    public void appendSubCategoryColumn(SubCategory currSubCategory) {
        int lastColumnIndex = buildBlankColumn();

        String subCategoryWeightPercentage = String.format("%.2f", 100 * currSubCategory.getWeight());
        String subCategoryNonWeightedPercentage = String.format("%.2f", 100 * currSubCategory.getRawFinalScore());

        for(int rowIndex = 0; rowIndex < gradingSchemeRowCount; rowIndex++) {
            JPanel currPanel = schemeGrid.get(rowIndex).get(lastColumnIndex);
            JTextField currTextField = new JTextField();
            currTextField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {

                }

                @Override
                public void focusLost(FocusEvent e) {
                    rerenderGradeValues();
                }
            });

            switch(rowIndex) {
                case 0:
                    break;
                case 1:
                    currPanel.setLayout(new BoxLayout(currPanel, BoxLayout.Y_AXIS));
                    // Show name and weight percentage
                    currPanel.add(new JLabel(currSubCategory.getName() + " (Weight)"));
                    currTextField.setText(subCategoryWeightPercentage);
                    currPanel.add(currTextField);
                    break;
                case 2:
                    currPanel.add(new JLabel(subCategoryNonWeightedPercentage));
                    break;
                case 3:
                    currPanel.setLayout(new BoxLayout(currPanel, BoxLayout.Y_AXIS));
                    currTextField.setText(Double.toString(currSubCategory.getPointsGained()));
                    currPanel.add(currTextField);
                    break;
                case 4:
                    currPanel.setLayout(new BoxLayout(currPanel, BoxLayout.Y_AXIS));
                    currTextField.setText(Double.toString(currSubCategory.getTotalPoints()));
                    currPanel.add(currTextField);
                    break;
                default:
                    break;
            }
        }

        JPanel firstRowPanel = schemeGrid.get(0).get(lastColumnIndex);
        subCategoryColMapping.put(firstRowPanel, currSubCategory);
    }

    /**
     * Given a CourseCategory, generates a new column with CourseCategory information.
     * Based on our mockups, this is primarily just the first row, but can be extended to fill in other rows as well.
     *
     * @param currCategory
     */
    public void appendCategoryColumn(CourseCategory currCategory) {
        // Since we're appending a column, we can get the column index we want to add to by simply getting the last column.
        int lastColumnIndex = buildBlankColumn();

        String categoryWeightPercentage = String.format("%.2f", 100 * currCategory.getWeight());

        for(int rowIndex = 0; rowIndex < gradingSchemeRowCount; rowIndex++) {
            JPanel currPanel = schemeGrid.get(rowIndex).get(lastColumnIndex);
            JTextField currTextField = new JTextField();
            currTextField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {

                }

                @Override
                public void focusLost(FocusEvent e) {
                    rerenderGradeValues();
                }
            });

            switch(rowIndex) {
                case 0:
                    currPanel.setLayout(new BoxLayout(currPanel, BoxLayout.Y_AXIS));
                    currPanel.add(new JLabel(currCategory.getName() + " (Weight)"));
                    currTextField.setText(categoryWeightPercentage);
                    currPanel.add(currTextField);
                case 1:
                    break;
                case 2:
                    currPanel.add(new JLabel(Double.toString(currCategory.getCategoryFinalWeightedScore())));
                    break;
                case 3:
                    break;
                case 4:
                    break;
                default:
                    break;
            }
        }

        JPanel firstRowPanel = schemeGrid.get(0).get(lastColumnIndex);
        courseCategoryColMapping.put(firstRowPanel, currCategory);
    }

    /**
     * Given rowTexts of text we want to put at each row, generates a new column filled with text at each row.
     * @param rowTexts
     */
    public void appendTextColumn(String[] rowTexts) {
        int lastColumnIndex = buildBlankColumn();

        // Style and add the text for each JPanel.
        for(int i = 0; i < schemeGrid.size(); i++) {
            JPanel currPanel = schemeGrid.get(i).get(lastColumnIndex);

            String currText = rowTexts[i];
            currPanel.add(new JLabel(currText));
        }
    }

    /**
     * Because we're using ArrayList of JPanels to add columns dynamically, we need to add a column of JPanels.
     * This creates a new blank column of schemeGrid, then styled accordingly for background color.
     *
     * @return int that is the last column index (0-indexed).
     */
    public int buildBlankColumn() {
        // First build the number of rows with blank JPanels.
        for(int i = 0; i < gradingSchemeRowCount; i++) {
            JPanel currPanel = new JPanel();
            currPanel.setPreferredSize(new Dimension(150, 50));

            schemeGrid.get(i).add(currPanel);
        }

        // Style the panels (row coloring).
        for(int i = 0; i < schemeGrid.size(); i++) {
            for(int j = 0; j < schemeGrid.get(i).size(); j++) {
                JPanel currPanel = schemeGrid.get(i).get(j);

                // Styling to make the table look like the mockup.
                if(i == 0) {
                    currPanel.setBackground(LIGHT_GRAY_COLOR);
                } else if (i == 1) {
                    currPanel.setBackground(DARK_GRAY_COLOR);
                } else if (i == 2) {
                    currPanel.setBackground(LIGHT_BLUE_COLOR);
                } else if (i == 3) {
                    currPanel.setBackground(LIGHT_GREEN_COLOR);
                } else {
                    currPanel.setBackground(Color.WHITE);
                }

                currPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, BORDER_COLOR));
                parentPanel.add(currPanel);
            }
        }

        return schemeGrid.get(0).size()-1;
    }

    public OverallGrade getOverallGradeFromFields() {
        OverallGrade overallGradeFromFields = new OverallGrade();

        // for each Category column (will need some index offset jumping)
        for (int columnIndex = 0; columnIndex < schemeGrid.get(0).size(); columnIndex++) {
            JPanel currPanel = schemeGrid.get(0).get(columnIndex);

            // If it's a Course Category column
            if (courseCategoryColMapping.containsKey(currPanel)) {
                // Create CourseCategory object of weight from textfield and getName
                CourseCategory currCategory = courseCategoryColMapping.get(currPanel);

                double categoryWeight = 0;

                // Get TextField with weight.
                for (Component c : currPanel.getComponents()) {
                    if (c instanceof JTextField) {
                        categoryWeight = Double.parseDouble(((JTextField) c).getText());
                    }
                }
                CourseCategory categoryFromFields = new CourseCategory(currCategory.getName(), categoryWeight);

                categoryFromFields.setWeight(categoryWeight/100);

                // for each subcategory within CourseCategory (we can make this assumption since SubCategory cannot be edited from any of these pages)
                for (int subCategoryIndex = 0; subCategoryIndex < currCategory.getSubCategoryList().size(); subCategoryIndex++) {
                    SubCategory currSubCategory = currCategory.getSubCategoryList().get(subCategoryIndex);
                    SubCategory subCategoryFromFields = new SubCategory(currSubCategory.getName());

                    // for each row
                    for (int rowIndex = 0; rowIndex < gradingSchemeRowCount; rowIndex++) {
                        JPanel rowPanel = schemeGrid.get(rowIndex).get(columnIndex+subCategoryIndex+1);
                        // Get TextField with value.
                        for (Component c : rowPanel.getComponents()) {
                            if (c instanceof JTextField) {
                                double subCategoryField = Double.parseDouble(((JTextField) c).getText());

                                switch (rowIndex) {
                                    case 0:
                                        break;
                                    case 1:
                                        // Row 1 is the weight.
                                        subCategoryFromFields.setWeight(subCategoryField/100);
                                        break;
                                    case 2:
                                        // Row 2 is the raw final score but this is not a JTextField.
                                        break;
                                    case 3:
                                        subCategoryFromFields.setPointsGained(subCategoryField);
                                        break;
                                    case 4:
                                        subCategoryFromFields.setTotalPoints(subCategoryField);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }

                    categoryFromFields.addSubCategory(subCategoryFromFields);
                }
                overallGradeFromFields.addCourseCategory(categoryFromFields);
            }
        }
        return overallGradeFromFields;
    }

    /**
     * Helper function to help a matching CourseCategory from this.modifiedGradeScheme.
     *
     * Used in the context of rerendering:
     *  When we rerender, we go through each column and the CourseCategory and rerender it with this.modifiedGradeScheme's CourseCategory
     *
     * @param courseCategory
     * @return
     */
    public CourseCategory findMatchingCategory(CourseCategory courseCategory) {
        for(int i = 0 ; i < this.modifiedGradeScheme.getCourseCategoryList().size(); i++) {
            if(courseCategory.getName() == this.modifiedGradeScheme.getCourseCategoryList().get(i).getName()) {
                return this.modifiedGradeScheme.getCourseCategoryList().get(i);
            }
        }
        return null;
    }

    /**
     * When the user clicks away from the JTextField, we need to update all the appropriate JLabels.
     */
    public void rerenderGradeValues() {
        this.modifiedGradeScheme = getOverallGradeFromFields();

        // Manually update Final Grade column for now
        JPanel currPanel = schemeGrid.get(2).get(1);
        for (Component c : currPanel.getComponents()) {
            if (c instanceof JLabel) {
                // Row 2 is the category final score, update it
                ((JLabel) c).setText(Double.toString(this.modifiedGradeScheme.getOverallGrade()));
            }
        }

        for (int columnIndex = 0; columnIndex < schemeGrid.get(0).size(); columnIndex++) {
            currPanel = schemeGrid.get(0).get(columnIndex);

            // If it's a Course Category column
            if (courseCategoryColMapping.containsKey(currPanel)) {
                // Create CourseCategory object of weight from textfield and getName
                CourseCategory currCategory = courseCategoryColMapping.get(currPanel);

                CourseCategory categoryToUpdateWith = findMatchingCategory(currCategory);
                double categoryFinalWeightedScore = categoryToUpdateWith.getCategoryFinalWeightedScore();

                // for each row
                for (int rowIndex = 0; rowIndex < gradingSchemeRowCount; rowIndex++) {
                    JPanel rowPanel = schemeGrid.get(rowIndex).get(columnIndex);
                    // Get TextField with value.
                    for (Component c : rowPanel.getComponents()) {
                        if (c instanceof JLabel) {
                            if(rowIndex == 2) {
                                // Row 2 is the category final score, update it
                                ((JLabel) c).setText(Double.toString(categoryFinalWeightedScore));
                            }
                        }
                    }
                }
                courseCategoryColMapping.put(currPanel, categoryToUpdateWith);

                // for each subcategory within CourseCategory (we can make this assumption since SubCategory cannot be edited from any of these pages)
                for (int subCategoryIndex = 0; subCategoryIndex < categoryToUpdateWith.getSubCategoryList().size(); subCategoryIndex++) {
                    SubCategory currSubCategory = categoryToUpdateWith.getSubCategoryList().get(subCategoryIndex);

                    // for each row
                    for (int rowIndex = 0; rowIndex < gradingSchemeRowCount; rowIndex++) {
                        JPanel rowPanel = schemeGrid.get(rowIndex).get(columnIndex+subCategoryIndex+1);
                        // Get JLabel with value.
                        for (Component c : rowPanel.getComponents()) {
                            if (c instanceof JLabel) {
                                double subCategoryFinalWeightedScore = currSubCategory.getRawFinalScore();
                                if(rowIndex == 2) {
                                    ((JLabel) c).setText(Double.toString(subCategoryFinalWeightedScore));
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
