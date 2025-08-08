package pl.pronux.sokker.ui.widgets.tables;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import pl.pronux.sokker.comparators.PlayerComparator;
import pl.pronux.sokker.data.cache.Cache;
import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.model.League;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.PlayerStats;
import pl.pronux.sokker.model.SVNumberFormat;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.handlers.DisplayHandler;
import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.FlagsResources;
import pl.pronux.sokker.ui.resources.Fonts;
import pl.pronux.sokker.ui.resources.ImageResources;
import pl.pronux.sokker.ui.widgets.interfaces.IViewSort;

public class PlayersTable extends SVTable<Player> implements IViewSort<Player> {
	
	public static final int MATCH_INDEX_1ST = 22;
	public static final int MATCH_INDEX_2ND = MATCH_INDEX_1ST + 1;
	public static final int MATCH_INDEX_3RD = MATCH_INDEX_2ND + 1;

	public static final int MATCH_INDEX_1ST_NEXT = 26;
	public static final int MATCH_INDEX_2ND_NEXT = MATCH_INDEX_1ST_NEXT + 1;
	public static final int MATCH_INDEX_3RD_NEXT = MATCH_INDEX_2ND_NEXT + 1;

	private PlayerComparator comparator;
	
	public PlayersTable(Composite parent, int style) {
		super(parent, style);

		comparator = new PlayerComparator();
		comparator.setColumn(PlayerComparator.SURNAME);
		comparator.setDirection(PlayerComparator.ASCENDING);
		
		this.setLinesVisible(true);
		this.setHeaderVisible(true);
		this.setFont(ConfigBean.getFontTable());
		
		String[] titles = {
				"", 
				Messages.getString("table.name"), 
				Messages.getString("table.surname"), 
				Messages.getString("table.height"),
				//Messages.getString("table.weight"),
				//Messages.getString("table.bmi"),
				Messages.getString("table.value"), 
				Messages.getString("table.salary"), 
				Messages.getString("table.age"), 
				Messages.getString("table.form"), 
				Messages.getString("table.stamina"), 
				Messages.getString("table.pace"), 
				Messages.getString("table.technique"), 
				Messages.getString("table.passing"), 
				Messages.getString("table.keeper"), 
				Messages.getString("table.defender"), 
				Messages.getString("table.playmaker"), 
				Messages.getString("table.scorer"), 
				Messages.getString("table.discipline"), 
				Messages.getString("table.experience"), 
				Messages.getString("table.teamwork"), 
				Messages.getString("table.cards"), 
				Messages.getString("table.injury"), 
				Messages.getString("table.note.short"), 
				Messages.getString("table.1st"), 
				Messages.getString("table.2nd"), 
				Messages.getString("table.3rd"), 
				Messages.getString("table.nextTraining"), 
				Messages.getString("table.1st"), 
				Messages.getString("table.2nd"), 
				Messages.getString("table.3rd"), 
				"" 
		};

		for (int j = 0; j < titles.length; j++) {
			TableColumn column = new TableColumn(this, SWT.NONE);

			if (j > 2) {
				column.setAlignment(SWT.RIGHT);
			} else {
				column.setAlignment(SWT.LEFT);
			}

			column.setText(titles[j]);
			column.setResizable(false);
			column.setMoveable(false);
			// if (titles[j].equals(Messages.getString("table.value"))) {
			// column.setWidth(100);
			// } else if (titles[j].equals(Messages.getString("table.salary"))) {
			// column.setWidth(70);
			// } else
			if (titles[j].isEmpty()) {
				// column.setWidth(70);
				if (SettingsHandler.IS_LINUX) {
					column.pack();
				}
			} else {
				// column.setWidth(40);
				column.pack();
			}
		}
		
		this.addLabelsListener();
	}

	public void fill(List<Player> players) {
		int max = 0;
		// Turn off drawing to avoid flicker
		this.setRedraw(false);

		// We remove all the table entries, sort our
		// rows, then add the entries
		this.removeAll();
		Collections.sort(players, comparator);
		for (Player player : players) {
			max = player.getSkills().length - 1;
			TableItem item = new TableItem(this, SWT.NONE);
			int c = 0;
			item.setData(Player.class.getName(), player);
			item.setImage(c++, FlagsResources.getFlag(player.getCountryfrom()));
			
			if(!player.getSkills()[max].isPassTraining()) {
				item.setForeground(ColorResources.getDarkGray());
			}
			
			item.setText(c++, player.getName());
			item.setText(c++, player.getSurname());
			item.setText(c++, String.valueOf(player.getHeight()));
			//item.setText(c++, String.format("%.2f", player.getSkills()[max].getWeight()));
			//item.setText(c++, String.format("%.2f", player.getSkills()[max].getBmi()));
			item.setText(c++, player.getSkills()[max].getValue().formatIntegerCurrency());
			item.setText(c++, player.getSkills()[max].getSalary().formatIntegerCurrency());
			item.setText(c++, String.valueOf(player.getSkills()[max].getAge()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getForm()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getStamina()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getPace()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getTechnique()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getPassing()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getKeeper()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getDefender()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getPlaymaker()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getScorer()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getDiscipline()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getExperience()));
			item.setText(c++, String.valueOf(player.getSkills()[max].getTeamwork()));
			if (player.getSkills()[max].getCards() == 1) {
				item.setImage(c++, ImageResources.getImageResources("yellow_card.png")); 
			} else if (player.getSkills()[max].getCards() == 2) {
				item.setImage(c++, ImageResources.getImageResources("2_yellow_cards.png")); 
			} else if (player.getSkills()[max].getCards() >= 3) {
				item.setImage(c++, ImageResources.getImageResources("red_card.png")); 
			} else {
				c++;
			}

			if (player.getSkills()[max].getInjurydays() > 0) {
				item.setImage(c, ImageResources.getImageResources("injury.png")); 
				item.setText(c++, BigDecimal.valueOf(player.getSkills()[max].getInjurydays()).setScale(0, BigDecimal.ROUND_UP).toString());
			} else {
				c++;
			}
			
			if (player.getNote() != null) {
				if (player.getNote().isEmpty()) {
					c++;
				} else {
					item.setImage(c++, ImageResources.getImageResources("note.png")); 
				}
			}
			
			if (player.getPlayerMatchStatistics() != null) {
				int week = Cache.getDate().getSokkerDate().getWeek();
				for (PlayerStats playerStats : player.getPlayerMatchStatistics()) {
					if ((playerStats.getMatch().getWeek() == week && playerStats.getMatch().getDay() < 6) ||
						(playerStats.getMatch().getWeek() == week - 1 && playerStats.getMatch().getDay() == 6)) {
						
						if (playerStats.getFormation() >= 0 && playerStats.getFormation() <= 4 && playerStats.getTimePlayed() > 0) {
							League league = playerStats.getMatch().getLeague();
							int matchDay = playerStats.getMatch().getDay();

							int idx = 0;
							if (matchDay == 6) {
								idx = MATCH_INDEX_1ST;
							} else if (matchDay == 1) {
								idx = MATCH_INDEX_2ND;
							} else if (matchDay == 4) {
								idx = MATCH_INDEX_3RD;
							}

							if (idx > 0) {
								if ((league.getType() == League.TYPE_LEAGUE || league.getType() == League.TYPE_PLAYOFF) && league.getIsOfficial() == League.OFFICIAL) {
									item.setFont(idx, Fonts.getBoldFont(DisplayHandler.getDisplay(), item.getFont(idx).getFontData()));
								}
								item.setText(idx, String.format("%s (%d')", Messages.getString("formation." + playerStats.getFormation()) , playerStats.getTimePlayed()));
								if (playerStats.getFormation() == PlayerStats.GK) {
									item.setBackground(idx, Colors.getPositionGK());
								} else if (playerStats.getFormation() == PlayerStats.DEF) {
									item.setBackground(idx, Colors.getPositionDEF());
								} else if (playerStats.getFormation() == PlayerStats.MID) {
									item.setBackground(idx, Colors.getPositionMID());
								} else if (playerStats.getFormation() == PlayerStats.ATT) {
									item.setBackground(idx, Colors.getPositionATT());
								}
							}
							
						}
					}
				}
			} else {
				item.setText(MATCH_INDEX_1ST, ""); 
				item.setText(MATCH_INDEX_2ND, ""); 
				item.setText(MATCH_INDEX_3RD, ""); 
			}

			if (player.getPlayerMatchStatistics() != null) {
				int week = Cache.getDate().getSokkerDate().getWeek();
				for (PlayerStats playerStats : player.getPlayerMatchStatistics()) {
					if ((playerStats.getMatch().getWeek() == week + 1 && playerStats.getMatch().getDay() < 6) ||
						(playerStats.getMatch().getWeek() == week && playerStats.getMatch().getDay() == 6)) {
						
						if (playerStats.getFormation() >= 0 && playerStats.getFormation() <= 4 && playerStats.getTimePlayed() > 0) {
							League league = playerStats.getMatch().getLeague();
							int matchDay = playerStats.getMatch().getDay();

							int idx = 0;
							if (matchDay == 6) {
								idx = MATCH_INDEX_1ST_NEXT;
							} else if (matchDay == 1) {
								idx = MATCH_INDEX_2ND_NEXT;
							} else if (matchDay == 4) {
								idx = MATCH_INDEX_3RD_NEXT;
							}

							if (idx > 0) {
								if ((league.getType() == League.TYPE_LEAGUE || league.getType() == League.TYPE_PLAYOFF) && league.getIsOfficial() == League.OFFICIAL) {
									item.setFont(idx, Fonts.getBoldFont(DisplayHandler.getDisplay(), item.getFont(idx).getFontData()));
								}
								item.setText(idx, String.format("%s (%d')", Messages.getString("formation." + playerStats.getFormation()) , playerStats.getTimePlayed()));
								if (playerStats.getFormation() == PlayerStats.GK) {
									item.setBackground(idx, Colors.getPositionGK());
								} else if (playerStats.getFormation() == PlayerStats.DEF) {
									item.setBackground(idx, Colors.getPositionDEF());
								} else if (playerStats.getFormation() == PlayerStats.MID) {
									item.setBackground(idx, Colors.getPositionMID());
								} else if (playerStats.getFormation() == PlayerStats.ATT) {
									item.setBackground(idx, Colors.getPositionATT());
								}
							}
							
						}
					}
				}
			} else {
				item.setText(MATCH_INDEX_1ST_NEXT, ""); 
				item.setText(MATCH_INDEX_2ND_NEXT, ""); 
				item.setText(MATCH_INDEX_3RD_NEXT, ""); 
			}
			
			if (max > 0) {
				//this.getChanges(player.getSkills()[max].getWeight(), player.getSkills()[max - 1].getWeight(), item, PlayerComparator.WEIGHT);
				//this.getChanges(player.getSkills()[max].getBmi(), player.getSkills()[max - 1].getBmi(), item, PlayerComparator.BMI);
				this.getChanges(player.getSkills()[max].getValue().toInt(), player.getSkills()[max - 1].getValue().toInt(), item, PlayerComparator.VALUE);
				this.getChanges(player.getSkills()[max].getSalary().toInt(), player.getSkills()[max - 1].getSalary().toInt(), item, PlayerComparator.SALARY);
				this.getChanges(player.getSkills()[max].getAge(), player.getSkills()[max - 1].getAge(), item, PlayerComparator.AGE);
				this.getChanges(player.getSkills()[max].getForm(), player.getSkills()[max - 1].getForm(), item, PlayerComparator.FORM);
				this.getChanges(player.getSkills()[max].getStamina(), player.getSkills()[max - 1].getStamina(), item, PlayerComparator.STAMINA);
				this.getChanges(player.getSkills()[max].getPace(), player.getSkills()[max - 1].getPace(), item, PlayerComparator.PACE);
				this.getChanges(player.getSkills()[max].getTechnique(), player.getSkills()[max - 1].getTechnique(), item, PlayerComparator.TECHNIQUE);
				this.getChanges(player.getSkills()[max].getPassing(), player.getSkills()[max - 1].getPassing(), item, PlayerComparator.PASSING);
				this.getChanges(player.getSkills()[max].getKeeper(), player.getSkills()[max - 1].getKeeper(), item, PlayerComparator.KEEPER);
				this.getChanges(player.getSkills()[max].getDefender(), player.getSkills()[max - 1].getDefender(), item, PlayerComparator.DEFENDER);
				this.getChanges(player.getSkills()[max].getPlaymaker(), player.getSkills()[max - 1].getPlaymaker(), item, PlayerComparator.PLAYMAKER);
				this.getChanges(player.getSkills()[max].getScorer(), player.getSkills()[max - 1].getScorer(), item, PlayerComparator.SCORER);
				this.getChanges(player.getSkills()[max].getDiscipline(), player.getSkills()[max - 1].getDiscipline(), item, PlayerComparator.DISCIPLINE);
				this.getChanges(player.getSkills()[max].getExperience(), player.getSkills()[max - 1].getExperience(), item, PlayerComparator.EXPERIENCE);
				this.getChanges(player.getSkills()[max].getTeamwork(), player.getSkills()[max - 1].getTeamwork(), item, PlayerComparator.TEAMWORK);
			} else {
				item.setBackground(1, ConfigBean.getColorNewTableObject());
				item.setBackground(2, ConfigBean.getColorNewTableObject());
			}
			if (player.getTransferList() > 0) {
				item.setBackground(1, ConfigBean.getColorTransferList());
				item.setBackground(2, ConfigBean.getColorTransferList());
			}
		}
		for (int i = 0; i < this.getColumnCount() - 1; i++) {
			this.getColumn(i).pack();
			// table.getColumn(i).setWidth(table.getColumn(i).getWidth());
		}

		// table.getColumn(PlayerComparator.CARDS).setWidth(30);
		// table.getColumn(PlayerComparator.NOTE).setWidth(30);
		// Turn drawing back on
		this.setRedraw(true);
	}


	public void filterTable(String text) {
		if (text.equalsIgnoreCase(Messages.getString("view.jumps"))) { 
			for (int i = 0; i < this.getItemCount(); i++) {
				for (int j = 7; j < this.getColumnCount(); j++) {
					if (this.getItem(i).getBackground(j).equals(ConfigBean.getColorIncrease()) || this.getItem(i).getBackground(j).equals(ConfigBean.getColorDecrease())) {
						break;
					}
					if (j == this.getColumnCount() - 1) {
						this.remove(i);
						i--;
					}
				}
			}
		}
	}

	public PlayerComparator getComparator() {
		return comparator;
	}
	
	@Override
	public void setLabel(Label label, int column, TableItem item) {
		if (column >= PlayerComparator.VALUE &&  column <= PlayerComparator.TEAMWORK) {
			Player player = (Player) item.getData(Player.class.getName());
			int maxSkill = player.getSkills().length - 1;
			int[] temp1 = player.getSkills()[maxSkill].getStatsTable();
			if (maxSkill > 0) {
				int[] temp2 = player.getSkills()[maxSkill - 1].getStatsTable();
				if (column >= PlayerComparator.FORM) {
					if (temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE] > 0) {
						label.setText(Messages.getString("skill.a" + temp1[column - PlayerComparator.VALUE]) + " (" + SVNumberFormat.formatIntegerWithSignZero(temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE]) + ")");
						label.setForeground(ConfigBean.getColorIncreaseDescription());
					} else if (temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE] < 0) {
						label.setText(Messages.getString("skill.a" + temp1[column - PlayerComparator.VALUE]) + " (" + SVNumberFormat.formatIntegerWithSignZero(temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE]) + ")");
						label.setForeground(ConfigBean.getColorDecreaseDescription());
					} else {
						label.setText(Messages.getString("skill.a" + temp1[column - PlayerComparator.VALUE]) + " (" + (temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE]) + ")");
					}

				} else {
					if (temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE] > 0) {
						label.setText(SVNumberFormat.formatIntegerWithSignZero(temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE]));
						label.setForeground(ConfigBean.getColorIncreaseDescription());
					} else if (temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE] < 0) {
						label.setText(SVNumberFormat.formatIntegerWithSignZero(temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE]));
						label.setForeground(ConfigBean.getColorDecreaseDescription());
					} else {
						label.setText(String.valueOf(temp1[column - PlayerComparator.VALUE] - temp2[column - PlayerComparator.VALUE]));
					}
				}
			} else {
				if (column >= PlayerComparator.FORM) {
					label.setText(Messages.getString("skill.a" + temp1[column - PlayerComparator.VALUE]) + " (0)");
				} else {
					label.setText("0");
				}
			}
			label.pack();
		} else if (column == PlayerComparator.NOTE) {
			int minSizeX = 200;
			int minSizeY = 80;

			int maxSizeX = 400;
			int maxSizeY = 200;

			Player player = (Player) item.getData(Player.class.getName());
			if (player.getNote() != null) {
				if (!player.getNote().isEmpty()) {
					label.setText(player.getNote());

					Point size = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);

					if (size.x < minSizeX) {
						size.x = minSizeX;
					}
					if (size.y < minSizeY) {
						size.y = minSizeY;
					}

					if (size.x > maxSizeX) {
						size.x = maxSizeX;
					}

					if (size.y > maxSizeY) {
						size.y = maxSizeY;
					}
					label.setSize(size);
				}
			}
		}
		super.setLabel(label, column, item);
	}

}
