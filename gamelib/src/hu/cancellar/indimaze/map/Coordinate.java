package hu.cancellar.indimaze.map;

public class Coordinate {
  public int x;
  public int y;

  public Coordinate(final int x, final int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Coordinate that = (Coordinate) o;

    if (this.x != that.x) return false;
    return this.y == that.y;

  }

  @Override
  public int hashCode() {
    int result = this.x;
    result = 31 * result + this.y;
    return result;
  }

  public boolean near(final Coordinate other, final int rad) {
    return Math.abs(other.x - this.x) <= rad && Math.abs(other.y - this.y) <= rad;
  }
}
