package hu.cancellar.indimaze.map;

import com.sun.org.apache.bcel.internal.generic.NOP;

import java.util.Random;

public class Player extends MapObject {

  static Random rand = new Random();
  static int nextId = 0;

  String id;
  Type characterType;
  int level;
  ActionType currentAction;
  ActionType realAction;
  int health = 12;
  int maxhealth = 12;
  private int ranking = 0;
  int killCount = 0;
  int deathCount = 0;
  int controlLevel = 20;

  public Player(final Type characterType) {
    super();
    nextId += 1;
    this.id = "internal-" + nextId;
    this.characterType = characterType;
    this.level = 1;
    this.currentAction = ActionType.NPC;
  }

  public Type getCharacterType() {
    return this.characterType;
  }

  public void setCharacterType(final Type characterType) {
    this.characterType = characterType;
  }

  public String getId() {
    return this.id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public int getLevel() {
    return this.level;
  }

  public void setLevel(final int level) {
    this.level = level;
  }

  public ActionType getCurrentAction() {
    return this.currentAction;
  }

  public void setCurrentAction(final ActionType currentAction) {
    this.currentAction = currentAction;
  }

  public void UpdateRealAction() {

    if (this.currentAction == ActionType.NOPE) {
      controlLevel--;
      if (controlLevel <= 0) {
        this.currentAction = ActionType.NPC;
      }
    } else {
      controlLevel = 20;
    }

    this.realAction = this.currentAction;
    if (this.realAction == ActionType.NPC) {
      this.realAction = ActionType.values()[rand.nextInt(4)];
    }
  }

  public boolean hitBy(final Player pp, final Level currentLevel, final Coordinate currCoord) {
    int base = pp.getCurrentAction() == ActionType.NPC ? 1 : 3;
    this.health = this.health - (rand.nextInt(2) + base);
    System.out.println("HP @("+pp.id+") >> (" + id + ")  => " + health);
    if (this.health <= 0) {
      deathCount++;
      this.health = this.maxhealth;
      currentLevel.respawn(this);
      return true;
    }
    return false;
  }

  public ActionType getRealAction() {
    return this.realAction;
  }

  public void ClearAction() {
    if (this.currentAction != ActionType.NPC) {
      this.currentAction = ActionType.NOPE;
    }
  }

  public int getHealth() {
    return health;
  }

  public int getMaxhealth() {
    return maxhealth;
  }

  public int getRanking() {
    return ranking;
  }

  public static void setRand(Random rand) {
    Player.rand = rand;
  }

  public void incKillCount() {
    killCount++;
  }

  public int getKillCount() {
    return killCount;
  }

  public int getDeathCount() {
    return deathCount;
  }

  public int getWeightedCount() {
    return killCount - deathCount * 3;
  }

  public void incLife(int amount) {
    System.out.println(id + " hp++ " + health + " + " + amount);
    health+= amount;
    if (health > maxhealth) {
      health = maxhealth;
    }
  }

  public void setRanking(int ranking) {
    this.ranking = ranking;
  }

  public enum ActionType {UP, DOWN, LEFT, RIGHT, NOPE, NPC}

  public enum Type {FAIRY, FARMER, COW}
}
