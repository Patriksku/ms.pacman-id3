package dataRecording;

import pacman.game.Constants;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class DataTuple {

	public enum DiscreteTag {
		VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH, NONE;

		public static DiscreteTag DiscretizeDouble(double aux) {
			if (aux < 0.1)
				return DiscreteTag.VERY_LOW;
			else if (aux <= 0.3)
				return DiscreteTag.LOW;
			else if (aux <= 0.5)
				return DiscreteTag.MEDIUM;
			else if (aux <= 0.7)
				return DiscreteTag.HIGH;
			else
				return DiscreteTag.VERY_HIGH;
		}
	}

	public MOVE DirectionChosen;

	// General game state this - not normalized!
	public int mazeIndex;
	public int currentLevel;
	public int pacmanPosition;
	public int pacmanLivesLeft;
	public int currentScore;
	public int totalGameTime;
	public int currentLevelTime;
	public int numOfPillsLeft;
	public int numOfPowerPillsLeft;
	public int blinkyIndex;
	public int inkyIndex;
	public int pinkyIndex;
	public int sueIndex;
	public boolean isJunction;
	public boolean moveRight = false;
	public boolean moveLeft= false;
	public boolean moveUp= false;
	public boolean moveDown= false;
	public MOVE lastMove;

	// Ghost this, dir, dist, edible - BLINKY, INKY, PINKY, SUE
	public boolean isBlinkyEdible = false;
	public boolean isInkyEdible = false;
	public boolean isPinkyEdible = false;
	public boolean isSueEdible = false;

	public int blinkyDist = -1;
	public int inkyDist = -1;
	public int pinkyDist = -1;
	public int sueDist = -1;
	public MOVE closestPillDir;
	public int closestPillDist;

	public MOVE blinkyDir;
	public MOVE inkyDir;
	public MOVE pinkyDir;
	public MOVE sueDir;

	// Util data - useful for normalization
	public int numberOfNodesInLevel;
	public int numberOfTotalPillsInLevel;
	public int numberOfTotalPowerPillsInLevel;
	private int maximumDistance = 150;

	public DataTuple(Game game, MOVE move) {
		if (move == MOVE.NEUTRAL) {
			move = game.getPacmanLastMoveMade();
		}

		this.DirectionChosen = move;

		this.mazeIndex = game.getMazeIndex();
		this.currentLevel = game.getCurrentLevel();
		this.pacmanPosition = game.getPacmanCurrentNodeIndex();
		this.pacmanLivesLeft = game.getPacmanNumberOfLivesRemaining();
		this.currentScore = game.getScore();
		this.totalGameTime = game.getTotalTime();
		this.currentLevelTime = game.getCurrentLevelTime();
		this.numOfPillsLeft = game.getNumberOfActivePills();
		this.numOfPowerPillsLeft = game.getNumberOfActivePowerPills();
		this.blinkyIndex = game.getGhostCurrentNodeIndex(GHOST.BLINKY);
		this.inkyIndex = game.getGhostCurrentNodeIndex(GHOST.INKY);
		this.pinkyIndex = game.getGhostCurrentNodeIndex(GHOST.PINKY);
		this.sueIndex = game.getGhostCurrentNodeIndex(GHOST.SUE);
		this.isJunction = game.isJunction(this.pacmanPosition);

		MOVE[] moves = game.getPossibleMoves(this.pacmanPosition);

		for(MOVE moveTemp: moves) {
			switch (moveTemp) {
				case LEFT:
					this.moveLeft = true;
					break;
				case RIGHT:
					this.moveRight = true;
					break;
				case UP:
					this.moveUp = true;
					break;
				case DOWN:
					this.moveDown = true;
					break;
			}
		}

		lastMove = game.getPacmanLastMoveMade();
		if (game.getGhostLairTime(GHOST.BLINKY) == 0) {
			this.isBlinkyEdible = game.isGhostEdible(GHOST.BLINKY);
			this.blinkyDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.BLINKY));
		}

		if (game.getGhostLairTime(GHOST.INKY) == 0) {
			this.isInkyEdible = game.isGhostEdible(GHOST.INKY);
			this.inkyDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.INKY));
		}

		if (game.getGhostLairTime(GHOST.PINKY) == 0) {
			this.isPinkyEdible = game.isGhostEdible(GHOST.PINKY);
			this.pinkyDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.PINKY));
		}

		if (game.getGhostLairTime(GHOST.SUE) == 0) {
			this.isSueEdible = game.isGhostEdible(GHOST.SUE);
			this.sueDist = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.SUE));
		}

		int[] pills = game.getActivePillsIndices();
		int bestPill = pills[0];
		int bestPillDistance = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pills[0]);
		for(int i = 0; i < pills.length; i++){
			if(bestPillDistance > game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pills[i])){
				bestPill = pills[i];
				bestPillDistance = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), pills[i]);
			}
		}
		this.closestPillDist = bestPillDistance;
		this.closestPillDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), bestPill, DM.PATH);

		this.blinkyDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.BLINKY), DM.PATH);
		this.inkyDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.INKY), DM.PATH);
		this.pinkyDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.PINKY), DM.PATH);
		this.sueDir = game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.SUE), DM.PATH);

		this.numberOfNodesInLevel = game.getNumberOfNodes();
		this.numberOfTotalPillsInLevel = game.getNumberOfPills();
		this.numberOfTotalPowerPillsInLevel = game.getNumberOfPowerPills();
	}

	public DataTuple(String data) {
		String[] dataSplit = data.split(";");

		this.DirectionChosen = MOVE.valueOf(dataSplit[0]);

		this.mazeIndex = Integer.parseInt(dataSplit[1]);
		this.currentLevel = Integer.parseInt(dataSplit[2]);
		this.pacmanPosition = Integer.parseInt(dataSplit[3]);
		this.pacmanLivesLeft = Integer.parseInt(dataSplit[4]);
		this.currentScore = Integer.parseInt(dataSplit[5]);
		this.totalGameTime = Integer.parseInt(dataSplit[6]);
		this.currentLevelTime = Integer.parseInt(dataSplit[7]);
		this.numOfPillsLeft = Integer.parseInt(dataSplit[8]);
		this.numOfPowerPillsLeft = Integer.parseInt(dataSplit[9]);
		this.isBlinkyEdible = Boolean.parseBoolean(dataSplit[10]);
		this.isInkyEdible = Boolean.parseBoolean(dataSplit[11]);
		this.isPinkyEdible = Boolean.parseBoolean(dataSplit[12]);
		this.isSueEdible = Boolean.parseBoolean(dataSplit[13]);
		this.blinkyDist = Integer.parseInt(dataSplit[14]);
		this.inkyDist = Integer.parseInt(dataSplit[15]);
		this.pinkyDist = Integer.parseInt(dataSplit[16]);
		this.sueDist = Integer.parseInt(dataSplit[17]);
		this.blinkyDir = MOVE.valueOf(dataSplit[18]);
		this.inkyDir = MOVE.valueOf(dataSplit[19]);
		this.pinkyDir = MOVE.valueOf(dataSplit[20]);
		this.sueDir = MOVE.valueOf(dataSplit[21]);
		this.numberOfNodesInLevel = Integer.parseInt(dataSplit[22]);
		this.numberOfTotalPillsInLevel = Integer.parseInt(dataSplit[23]);
		this.numberOfTotalPowerPillsInLevel = Integer.parseInt(dataSplit[24]);
		this.isJunction = Boolean.parseBoolean(dataSplit[25]);
		this.moveRight = Boolean.parseBoolean(dataSplit[26]);
		this.moveLeft = Boolean.parseBoolean(dataSplit[27]);
		this.moveUp = Boolean.parseBoolean(dataSplit[28]);
		this.moveDown = Boolean.parseBoolean(dataSplit[29]);
		this.lastMove = MOVE.valueOf(dataSplit[30]);
		this.closestPillDir = MOVE.valueOf(dataSplit[31]);
		this.closestPillDist = Integer.parseInt(dataSplit[32]);

	}

	public String getSaveString() {
		StringBuilder stringbuilder = new StringBuilder();

		stringbuilder.append(this.DirectionChosen + ";");
		stringbuilder.append(this.mazeIndex + ";");
		stringbuilder.append(this.currentLevel + ";");
		stringbuilder.append(this.pacmanPosition + ";");
		stringbuilder.append(this.pacmanLivesLeft + ";");
		stringbuilder.append(this.currentScore + ";");
		stringbuilder.append(this.totalGameTime + ";");
		stringbuilder.append(this.currentLevelTime + ";");
		stringbuilder.append(this.numOfPillsLeft + ";");
		stringbuilder.append(this.numOfPowerPillsLeft + ";");
		stringbuilder.append(this.isBlinkyEdible + ";");
		stringbuilder.append(this.isInkyEdible + ";");
		stringbuilder.append(this.isPinkyEdible + ";");
		stringbuilder.append(this.isSueEdible + ";");
		stringbuilder.append(this.blinkyDist + ";");
		stringbuilder.append(this.inkyDist + ";");
		stringbuilder.append(this.pinkyDist + ";");
		stringbuilder.append(this.sueDist + ";");
		stringbuilder.append(this.blinkyDir + ";");
		stringbuilder.append(this.inkyDir + ";");
		stringbuilder.append(this.pinkyDir + ";");
		stringbuilder.append(this.sueDir + ";");
		stringbuilder.append(this.numberOfNodesInLevel + ";");
		stringbuilder.append(this.numberOfTotalPillsInLevel + ";");
		stringbuilder.append(this.numberOfTotalPowerPillsInLevel + ";");
		stringbuilder.append(this.isJunction + ";");
		stringbuilder.append(this.moveRight + ";");
		stringbuilder.append(this.moveLeft + ";");
		stringbuilder.append(this.moveUp + ";");
		stringbuilder.append(this.moveDown + ";");
		stringbuilder.append(this.lastMove + ";");
		stringbuilder.append(this.closestPillDir + ";");
		stringbuilder.append(this.closestPillDist + ";");


		return stringbuilder.toString();
	}

	/**
	 * Used to normalize distances. Done via min-max normalization. Supposes
	 * that minimum possible distance is 0. Supposes that the maximum possible
	 * distance is 150.
	 *
	 * @param dist
	 *            Distance to be normalized
	 * @return Normalized distance
	 */
	public double normalizeDistance(int dist) {
		return ((dist - 0) / (double) (this.maximumDistance - 0)) * (1 - 0) + 0;
	}

	public DiscreteTag discretizeDistance(int dist) {
		if (dist == -1)
			return DiscreteTag.NONE;
		double aux = this.normalizeDistance(dist);
		return DiscreteTag.DiscretizeDouble(aux);
	}

	public double normalizeLevel(int level) {
		return ((level - 0) / (double) (Constants.NUM_MAZES - 0)) * (1 - 0) + 0;
	}

	public double normalizePosition(int position) {
		return ((position - 0) / (double) (this.numberOfNodesInLevel - 0)) * (1 - 0) + 0;
	}

	public DiscreteTag discretizePosition(int pos) {
		double aux = this.normalizePosition(pos);
		return DiscreteTag.DiscretizeDouble(aux);
	}

	public double normalizeBoolean(boolean bool) {
		if (bool) {
			return 1.0;
		} else {
			return 0.0;
		}
	}

	public double normalizeNumberOfPills(int numOfPills) {
		return ((numOfPills - 0) / (double) (this.numberOfTotalPillsInLevel - 0)) * (1 - 0) + 0;
	}

	public DiscreteTag discretizeNumberOfPills(int numOfPills) {
		double aux = this.normalizeNumberOfPills(numOfPills);
		return DiscreteTag.DiscretizeDouble(aux);
	}

	public double normalizeNumberOfPowerPills(int numOfPowerPills) {
		return ((numOfPowerPills - 0) / (double) (this.numberOfTotalPowerPillsInLevel - 0)) * (1 - 0) + 0;
	}

	public DiscreteTag discretizeNumberOfPowerPills(int numOfPowerPills) {
		double aux = this.normalizeNumberOfPowerPills(numOfPowerPills);
		return DiscreteTag.DiscretizeDouble(aux);
	}

	public double normalizeTotalGameTime(int time) {
		return ((time - 0) / (double) (Constants.MAX_TIME - 0)) * (1 - 0) + 0;
	}

	public DiscreteTag discretizeTotalGameTime(int time) {
		double aux = this.normalizeTotalGameTime(time);
		return DiscreteTag.DiscretizeDouble(aux);
	}

	public double normalizeCurrentLevelTime(int time) {
		return ((time - 0) / (double) (Constants.LEVEL_LIMIT - 0)) * (1 - 0) + 0;
	}

	public DiscreteTag discretizeCurrentLevelTime(int time) {
		double aux = this.normalizeCurrentLevelTime(time);
		return DiscreteTag.DiscretizeDouble(aux);
	}

	/**
	 *
	 * Max score value lifted from highest ranking PacMan controller on PacMan
	 * vs Ghosts website: http://pacman-vs-ghosts.net/controllers/1104
	 *
	 * @param score
	 * @return
	 */
	public double normalizeCurrentScore(int score) {
		return ((score - 0) / (double) (82180 - 0)) * (1 - 0) + 0;
	}

	public DiscreteTag discretizeCurrentScore(int score) {
		double aux = this.normalizeCurrentScore(score);
		return DiscreteTag.DiscretizeDouble(aux);
	}

	public String discreteDistance(int distance) {
		String returnString = "";
		if(distance <= 20 && distance >= 0) {
			returnString = "LOW";
		}
		else if(distance > 20 && distance < 50) {
			returnString = "MEDIUM";
		}
		else {
			returnString = "HIGH";
		}
		return returnString;
	}

	public String discreteBoolean(boolean b) {
		if(b) return "YES";
		else return "NO";
	}

	public String getAttributeValue(String attribute) {
		String returnString = "";

		switch(attribute) {
			case "isBlinkyEdible":
				returnString = discreteBoolean(this.isBlinkyEdible);
				break;
			case "isInkyEdible":
				returnString = discreteBoolean(this.isInkyEdible);
				break;
			case "isPinkyEdible":
				returnString = discreteBoolean(this.isPinkyEdible);
				break;
			case "isSueEdible":
				returnString = discreteBoolean(this.isSueEdible);
				break;
			case "blinkyDist":
//				returnString = discreteDistance(this.blinkyDist);
				returnString = discretizeDistance(this.blinkyDist).toString();
				break;
			case "inkyDist":
//				returnString = discreteDistance(this.inkyDist);
				returnString = discretizeDistance(this.inkyDist).toString();
				break;
			case "pinkyDist":
//				returnString = discreteDistance(this.pinkyDist);
				returnString = discretizeDistance(this.pinkyDist).toString();
				break;
			case "sueDist":
//				returnString = discreteDistance(this.sueDist);
				returnString = discretizeDistance(this.sueDist).toString();
				break;
			case "blinkyDir":
				returnString = this.blinkyDir.toString();
				break;
			case "inkyDir":
				returnString = this.inkyDir.toString();
				break;
			case "pinkyDir":
				returnString = this.pinkyDir.toString();
				break;
			case "sueDir":
				returnString = this.sueDir.toString();
				break;
			case "powerPillClose":

				break;
			case "isJunction":
				returnString = discreteBoolean(this.isJunction);
				break;

			case "pacmanPosition":
				returnString = discretizePosition(pacmanPosition).toString();
				break;
			case "moveLeft":
				returnString = discreteBoolean(this.moveLeft);
				break;
			case "moveRight":
				returnString = discreteBoolean(this.moveRight);
				break;
			case "moveDown":
				returnString = discreteBoolean(this.moveDown);
				break;
			case "moveUp":
				returnString = discreteBoolean(this.moveUp);
				break;
			case "lastMove":
				returnString = lastMove.toString();
				break;
			case "closestPillDir":
				returnString = closestPillDir.toString();
				break;
			case "closestPillDist":
				returnString = discretizeDistance(closestPillDist).toString();
				break;
			case "directionChosen":
				returnString = this.DirectionChosen.toString();
				break;
			case "blinkyVun":
				if((getAttributeValue("blinkyDist").equals("VERY_LOW") || getAttributeValue("blinkyDist").equals("VERY_LOW")) && isBlinkyEdible){
					returnString = "vun";
				}else{
					returnString = "notVun";
				}
				break;
			case "inkyVun":
				if((getAttributeValue("inkyDist").equals("VERY_LOW") || getAttributeValue("inkyDist").equals("VERY_LOW")) && isInkyEdible){
					returnString = "vun";
				}else{
					returnString = "notVun";
				}
				break;

			case "pinkyVun":
				if((getAttributeValue("pinkyDist").equals("VERY_LOW") || getAttributeValue("pinkyDist").equals("VERY_LOW")) && isPinkyEdible){
					returnString = "vun";
				}else{
					returnString = "notVun";
				}
				break;

			case "sueVun":
				if((getAttributeValue("sueDist").equals("VERY_LOW") || getAttributeValue("sueDist").equals("VERY_LOW")) && isSueEdible){
					returnString = "vun";
				}else{
					returnString = "notVun";
				}
				break;

		}
		return returnString;
	}


}
