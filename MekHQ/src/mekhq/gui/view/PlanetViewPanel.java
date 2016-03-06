/*
 * PlanetViewPanel
 *
 * Created on July 26, 2009, 11:32 PM
 */

package mekhq.gui.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import mekhq.campaign.Campaign;
import mekhq.campaign.universe.Planet;
import mekhq.campaign.universe.Star;

/**
 * A custom panel that gets filled in with goodies from a Planet record
 * @author  Jay Lawson <jaylawson39 at yahoo.com>
 */
public class PlanetViewPanel extends JPanel {
	private static final long serialVersionUID = 7004741688464105277L;

	private Star star;
	private Campaign campaign;
	
	private JPanel pnlNeighbors;
	private JPanel pnlStats;
	private JTextArea txtDesc;
	
	private JLabel lblOwner;
	private JLabel lblStarType;
	private JTextArea txtStarType;
	private JLabel lblRecharge;
	private JTextArea txtRecharge;

	public PlanetViewPanel(Star star, Campaign campaign) {
		this.star = star;
		this.campaign = campaign;
		initComponents();
	}
	
	private void initComponents() {
		GridBagConstraints gridBagConstraints;

		pnlStats = new JPanel();
		pnlNeighbors = new JPanel();
		txtDesc = new JTextArea();
		       
		setLayout(new GridBagLayout());

		setBackground(Color.WHITE);

		pnlStats.setName("pnlStats");
		pnlStats.setBorder(BorderFactory.createTitledBorder(star.getShortName(campaign.getDate())));
		pnlStats.setBackground(new Color(230, 230, 230));
		fillStats();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;	
		add(pnlStats, gridBagConstraints);
		
		pnlNeighbors.setName("pnlNeighbors");
		pnlNeighbors.setBorder(BorderFactory.createTitledBorder("Planets within 30 light years"));
		pnlNeighbors.setBackground(Color.WHITE);
		getNeighbors();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;	
		add(pnlNeighbors, gridBagConstraints);
		
		txtDesc.setName("txtDesc");
		txtDesc.setText(star.getDescription());
		txtDesc.setEditable(false);
		txtDesc.setLineWrap(true);
		txtDesc.setWrapStyleWord(true);
		txtDesc.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Description"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		add(txtDesc, gridBagConstraints);
	}

	private void getNeighbors() {
		GridBagConstraints gridBagConstraints;
		pnlNeighbors.setLayout(new GridBagLayout());
		int i = 0;
		JLabel lblNeighbor;
		JLabel lblDistance;
		for(Star neighbor : campaign.getAllReachableStarsFrom(star)) {
			if(neighbor.equals(star)) {
				continue;
			}
			lblNeighbor = new JLabel(neighbor.getPrintableName(campaign.getDate()) + " (" + neighbor.getFactionDesc(campaign.getDate()) + ")");
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = i;
			gridBagConstraints.gridwidth = 1;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			pnlNeighbors.add(lblNeighbor, gridBagConstraints);
			
			lblDistance = new JLabel(String.format(Locale.ROOT, "%.2f ly", star.getDistanceTo(neighbor)));
			lblDistance.setAlignmentX(Component.RIGHT_ALIGNMENT);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = i;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			pnlNeighbors.add(lblDistance, gridBagConstraints);

			++ i;
		}
		
	}
	
	private void fillPlanetPanel(JPanel panel, Planet planet) {
    	ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.PlanetViewPanel");
    	GridBagConstraints gridBagConstraints;
    	Date now = campaign.getDate();
    	int infoRow = 1;

    	panel.setLayout(new GridBagLayout());
    	
		if( null != planet.getSystemPosition() ) {
			JLabel lblPosition = new JLabel();
			lblPosition.setName("lblPosition"); // NOI18N
			lblPosition.setText(resourceMap.getString("lblPosition.text"));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(lblPosition, gridBagConstraints);
			
			JTextArea txtPosition = new JTextArea();
			txtPosition .setName("txtPosition"); // NOI18N
			txtPosition.setText(planet.getSystemPositionText());
			txtPosition.setEditable(false);
			txtPosition.setLineWrap(true);
			txtPosition.setWrapStyleWord(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			panel.add(txtPosition, gridBagConstraints);
			
			++ infoRow;
		}
		
		JLabel lblJumpPoint = new JLabel();
		lblJumpPoint.setName("lblJumpPoint"); // NOI18N
		lblJumpPoint.setText(resourceMap.getString("lblJumpPoint1.text"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = infoRow;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		panel.add(lblJumpPoint, gridBagConstraints);
		
		JTextArea txtJumpPoint = new JTextArea();
		txtJumpPoint.setName("lblJumpPoint2"); // NOI18N
		txtJumpPoint.setText(Double.toString(Math.round(100 * planet.getTimeToJumpPoint(1))/100.0) + " days");
		txtJumpPoint.setEditable(false);
		txtJumpPoint.setLineWrap(true);
		txtJumpPoint.setWrapStyleWord(true);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = infoRow;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.insets = new Insets(0, 10, 0, 0);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		panel.add(txtJumpPoint, gridBagConstraints);
		
		++ infoRow;
		
		JLabel lblSatellite = new JLabel();
		lblSatellite.setName("lblSatellite"); // NOI18N
		lblSatellite.setText(resourceMap.getString("lblSatellite1.text"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = infoRow;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		panel.add(lblSatellite, gridBagConstraints);
		
		JTextArea txtSatellite = new JTextArea();
		txtSatellite.setName("lblSatellite2"); // NOI18N
		txtSatellite.setText(planet.getSatelliteDescription());
		txtSatellite.setEditable(false);
		txtSatellite.setLineWrap(true);
		txtSatellite.setWrapStyleWord(true);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = infoRow;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.insets = new Insets(0, 10, 0, 0);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		panel.add(txtSatellite, gridBagConstraints);
		
		++ infoRow;
		
		if( null != planet.getGravity() ) {
			JLabel lblGravity = new JLabel();
			lblGravity.setName("lblGravity1"); // NOI18N
			lblGravity.setText(resourceMap.getString("lblGravity1.text"));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(lblGravity, gridBagConstraints);
			
			JTextArea txtGravity = new JTextArea();
			txtGravity.setName("lblGravity2"); // NOI18N
			txtGravity.setText(planet.getGravityText());
			txtGravity.setEditable(false);
			txtGravity.setLineWrap(true);
			txtGravity.setWrapStyleWord(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(txtGravity, gridBagConstraints);
			
			++ infoRow;
		}
		
		if( null != planet.getPressure(now) ) {
			JLabel lblPressure = new JLabel();
			lblPressure.setName("lblPressure1"); // NOI18N
			lblPressure.setText(resourceMap.getString("lblPressure1.text"));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(lblPressure, gridBagConstraints);
			
			JTextArea txtPressure = new JTextArea();
			txtPressure.setName("lblPressure2"); // NOI18N
			txtPressure.setText(planet.getPressureName(now));
			txtPressure.setEditable(false);
			txtPressure.setLineWrap(true);
			txtPressure.setWrapStyleWord(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(txtPressure, gridBagConstraints);
			
			++ infoRow;
		}
		
		if( null != planet.getTemperature(now) || null != planet.getClimate(now) ) {
			JLabel lblTemp = new JLabel();
			lblTemp.setName("lblTemp1"); // NOI18N
			lblTemp.setText(resourceMap.getString("lblTemp1.text"));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(lblTemp, gridBagConstraints);
			
			JTextArea txtTemp = new JTextArea();
			txtTemp.setName("lblTemp2"); // NOI18N
			String text;
			if( null == planet.getClimate(now) ) {
				text = planet.getTemperature(now) + "°C";
			} else if( null == planet.getTemperature(now) ) {
				text = "(" + planet.getClimateName(now) + ")";
			} else {
				text = planet.getTemperature(now) + "°C (" + planet.getClimateName(now) + ")";
			}
			txtTemp.setText(text);
			txtTemp.setEditable(false);
			txtTemp.setLineWrap(true);
			txtTemp.setWrapStyleWord(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(txtTemp, gridBagConstraints);
			
			++ infoRow;
		}
		
		if( null != planet.getPercentWater(now) ) {
			JLabel lblWater = new JLabel();
			lblWater.setName("lblWater1"); // NOI18N
			lblWater.setText(resourceMap.getString("lblWater1.text"));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(lblWater, gridBagConstraints);
			
			JTextArea txtWater = new JTextArea();
			txtWater.setName("lblWater2"); // NOI18N
			txtWater.setText(planet.getPercentWater(now) + " percent");
			txtWater.setEditable(false);
			txtWater.setLineWrap(true);
			txtWater.setWrapStyleWord(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(txtWater, gridBagConstraints);
			
			++ infoRow;
		}
		
		if( null != planet.getHPG(now) ) {
			JLabel lblHPG = new JLabel();
			lblHPG.setName("lblHPG1"); // NOI18N
			lblHPG.setText(resourceMap.getString("lblHPG1.text"));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(lblHPG, gridBagConstraints);
			
			JTextArea txtHPG = new JTextArea();
			txtHPG.setName("lblHPG2"); // NOI18N
			txtHPG.setText(planet.getHPGClass(now));
			txtHPG.setEditable(false);
			txtHPG.setLineWrap(true);
			txtHPG.setWrapStyleWord(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(txtHPG, gridBagConstraints);
			
			++ infoRow;
		}
		
		if( null != planet.getLifeForm(now) ) {
			JLabel lblAnimal = new JLabel();
			lblAnimal.setName("lblAnimal1"); // NOI18N
			lblAnimal.setText(resourceMap.getString("lblAnimal1.text"));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(lblAnimal, gridBagConstraints);
			
			JTextArea txtAnimal = new JTextArea();
			txtAnimal.setName("lblAnimal2"); // NOI18N
			txtAnimal.setText(planet.getLifeFormName(now));
			txtAnimal.setEditable(false);
			txtAnimal.setLineWrap(true);
			txtAnimal.setWrapStyleWord(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(txtAnimal, gridBagConstraints);
			
			++ infoRow;
		}
		
		if( null != planet.getSocioIndustrial(now) ) {
			JLabel lblSocioIndustrial = new JLabel();
			lblSocioIndustrial.setName("lblSocioIndustrial1"); // NOI18N
			lblSocioIndustrial.setText(resourceMap.getString("lblSocioIndustrial1.text"));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(lblSocioIndustrial, gridBagConstraints);
			
			JTextArea txtSocioIndustrial = new JTextArea();
			txtSocioIndustrial.setName("lblSocioIndustrial2"); // NOI18N
			txtSocioIndustrial.setText(planet.getSocioIndustrialText(now));
			txtSocioIndustrial.setEditable(false);
			txtSocioIndustrial.setLineWrap(true);
			txtSocioIndustrial.setWrapStyleWord(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(txtSocioIndustrial, gridBagConstraints);
			
			++ infoRow;
		}
		
		if( null != planet.getLandMasses() ) {
			JLabel lblLandMass = new JLabel();
			lblLandMass.setName("lblLandMass1"); // NOI18N
			lblLandMass.setText(resourceMap.getString("lblLandMass1.text"));
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(lblLandMass, gridBagConstraints);
			
			JTextArea txtLandMass = new JTextArea();
			txtLandMass.setName("lblLandMass2"); // NOI18N
			txtLandMass.setText(planet.getLandMassDescription());
			txtLandMass.setEditable(false);
			txtLandMass.setLineWrap(true);
			txtLandMass.setWrapStyleWord(true);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = infoRow;
			gridBagConstraints.weightx = 0.5;
			gridBagConstraints.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			panel.add(txtLandMass, gridBagConstraints);
			
			++ infoRow;
		}
	}

	private void fillStats() {
    	
    	ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.PlanetViewPanel");
    	
    	lblOwner = new JLabel();
    	lblStarType = new JLabel();
    	txtStarType = new JTextArea();
    	lblRecharge = new JLabel();
    	txtRecharge = new JTextArea();
    	
    	Date now = campaign.getDate();
    	int infoRow = 1;
    	
    	GridBagConstraints gridBagConstraints;
		pnlStats.setLayout(new GridBagLayout());
		
		lblOwner.setName("lblOwner"); // NOI18N
		lblOwner.setText("<html><b>" + star.getName(now) + "</b> <i>" + star.getFactionDesc(now) + "</i></html>");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new Insets(0, 0, 5, 0);
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		pnlStats.add(lblOwner, gridBagConstraints);
		
		lblStarType.setName("lblStarType"); // NOI18N
		lblStarType.setText(resourceMap.getString("lblStarType1.text"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = infoRow;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		pnlStats.add(lblStarType, gridBagConstraints);
		
		txtStarType.setName("lblStarType2"); // NOI18N
		txtStarType.setText(star.getSpectralTypeNormalized() + " (" + star.getRechargeTime() + " hours)");
		txtStarType.setEditable(false);
		txtStarType.setLineWrap(true);
		txtStarType.setWrapStyleWord(true);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = infoRow;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.insets = new Insets(0, 10, 0, 0);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		pnlStats.add(txtStarType, gridBagConstraints);
		
		++ infoRow;
		
		lblRecharge.setName("lblRecharge1"); // NOI18N
		lblRecharge.setText(resourceMap.getString("lblRecharge1.text"));
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = infoRow;
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		pnlStats.add(lblRecharge, gridBagConstraints);
		
		txtRecharge.setName("lblRecharge2"); // NOI18N
		txtRecharge.setText(star.getRechargeStations(now));
		txtRecharge.setEditable(false);
		txtRecharge.setLineWrap(true);
		txtRecharge.setWrapStyleWord(true);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = infoRow;
		gridBagConstraints.weightx = 0.5;
		gridBagConstraints.insets = new Insets(0, 10, 0, 0);
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
		pnlStats.add(txtRecharge, gridBagConstraints);
		
		++ infoRow;

		// Run through the planets
		for( int orbit = 1; orbit <= star.getNumPlanets(); ++ orbit ) {
			Planet planet = star.getPlanet(orbit);
			if( null != planet ) {
				JPanel planetPanel = new JPanel();
				planetPanel.setBorder(BorderFactory.createTitledBorder(planet.getName(now)));
				planetPanel.setBackground(Color.WHITE);
				gridBagConstraints = new GridBagConstraints();
				gridBagConstraints.gridx = 0;
				gridBagConstraints.gridy = infoRow;
				gridBagConstraints.gridwidth = 2;
				gridBagConstraints.weightx = 1.0;
				gridBagConstraints.insets = new Insets(0, 10, 0, 0);
				gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
				gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
				fillPlanetPanel(planetPanel, planet);
				pnlStats.add(planetPanel, gridBagConstraints);
				
				++ infoRow;
			}
		}
    }
}