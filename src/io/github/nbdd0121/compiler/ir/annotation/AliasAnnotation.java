package io.github.nbdd0121.compiler.ir.annotation;

import io.github.nbdd0121.compiler.ir.function.Local;

import java.util.HashSet;
import java.util.Objects;

public class AliasAnnotation extends Annotation {

	public static class Base {
		// int size;
	}

	public static class Location {
		/**
		 * Base of the memory that this pointer points to. null indicates that
		 * it is the whole addressable memory
		 */
		Base base;
		/**
		 * Offset relative to start of memory. No offset should be negative. -1
		 * means that we don't know where the pointer points to
		 */
		int offset;
		/**
		 * Size of the memory block that pointer points to. This must be
		 * positive.
		 */
		int size;

		public Location(Base base, int offset, int size) {
			this.base = base;
			this.offset = offset;
			this.size = size;
		}

		public static Location anywhere(int size) {
			return new Location(null, -1, size);
		}

		public static Location alloca(int size) {
			return new Location(new Base(), 0, size);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(base) ^ offset ^ size;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Location) {
				Location loc = (Location) obj;
				return Objects.equals(base, loc.base) && offset == loc.offset
						&& size == loc.size;
			}
			return false;
		}

		public boolean isInterfereWith(Location loc) {
			int upperBound = offset + size;
			int locUpperBound = loc.offset + loc.size;
			if (base == null && loc.base == null) {
				if (offset < 0 || loc.offset < 0) {
					/* Anywhere interfere with anywhere */
					return true;
				}
				if (offset <= loc.offset && loc.offset < upperBound
						|| offset <= locUpperBound
						&& locUpperBound < upperBound) {
					return true;
				}
				return false;
			} else if (base != null && loc.base != null) {
				/*
				 * Different memory block will definitely not interfere with
				 * each other
				 */
				if (base != loc.base) {
					return false;
				}
				if (offset < 0 || loc.offset < 0) {
					/* Anywhere interfere with anywhere */
					return true;
				}
				if (offset <= loc.offset && loc.offset < upperBound
						|| offset <= locUpperBound
						&& locUpperBound < upperBound) {
					return true;
				}
				return false;
			} else {
				/*
				 * We cannot judge if one is located somewhere in memory while
				 * another one is located in a specific range, so we make
				 * conservative assumptions
				 */
				return true;
			}
		}

		public boolean isDetermined() {
			return offset >= 0;
		}

	}

	public Location location;
	public HashSet<Local> interfereVariables = new HashSet<>();

}
