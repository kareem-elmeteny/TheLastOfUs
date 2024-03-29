package model.characters;

import java.awt.Point;
import java.util.ArrayList;

import model.world.CharacterCell;
import engine.Game;
import exceptions.InvalidTargetException;
import exceptions.NotEnoughActionsException;

public abstract class Character {
	private String name;
	private int maxHp;
	private int currentHp;
	private Point location;
	private int attackDmg;
	private Character target;
	protected ArrayList<CharacterListener> listeners = new ArrayList<>();

	public Character(String name, int maxHp, int attackDamage) {
		this.name = name;
		this.maxHp = maxHp;
		this.attackDmg = attackDamage;
		this.currentHp = maxHp;
	}

	public int getCurrentHp() {
		return currentHp;
	}

	public void setCurrentHp(int currentHp) {
		if (this.currentHp == currentHp) {
			return;
		}
		int oldValue = this.currentHp;
		if (currentHp <= 0) {
			this.currentHp = 0;
			onCharacterDeath();

		} else if (currentHp > maxHp) {
			this.currentHp = maxHp;
		} else {
			this.currentHp = currentHp;
		}
		for (CharacterListener listener : listeners) {
			listener.onChangedProperty(this, "currentHp", oldValue, this.currentHp);
		}
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public Character getTarget() {
		return target;
	}

	public void setTarget(Character target) {
		Character oldTarget = this.target;
		this.target = target;
		if (this == Game.getSelectedHero()) {
			Game.onTargetChanged(oldTarget, target);
		}
	}

	public String getName() {
		return name;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public int getAttackDmg() {
		return attackDmg;
	}

	public void attack() throws NotEnoughActionsException,
			InvalidTargetException {
		getTarget().setCurrentHp(getTarget().getCurrentHp() - getAttackDmg());
		Game.handleAttack(this, target);
		getTarget().defend(this);
	}

	public void defend(Character c) {
		c.setCurrentHp(c.getCurrentHp() - getAttackDmg() / 2);
		Game.handleDefend(this, c);
	}

	public void onCharacterDeath() {
		Point p = this.getLocation();

		if (this instanceof Zombie) {
			Game.zombies.remove(this);
			Game.spawnNewZombie();
		} else if (this instanceof Hero) {
			Game.removeHero((Hero) this);
		}
		if (p != null) {
			Game.setCell(p.x, p.y, new CharacterCell(null));
		}
	}

	public String getImage() {
		return "images/" + name.toLowerCase().replace(" ", "") + ".png";
	}

	public String getType(String s) {
		switch (s) {
			case "FIGH":
				return "Fighter";
			case "MED":
				return "Medic";
			case "EXP":
				return "Explorer";
			default:
				return "?";
		}
	}

	public String getHtmlDescription() {
		return "<html>"
				+ getName()
				+ "<br/>Maximum Health: <span color='green'>" + getMaxHp() + "</span>"
				+ "<br />Attack Damage: <span color='red'>" + getAttackDmg() + "</span>"
				+ "</html>";
	}

	public void addCharacterListener(CharacterListener listener) {
		listeners.add(listener);
	}

	public void removeCharacterListener(CharacterListener listener) {
		listeners.remove(listener);
	}

	public abstract String getDeathSound();
	public abstract String getDamagedSound();
}
