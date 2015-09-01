package amidst.bytedata.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import amidst.bytedata.ByteClassFinder;
import amidst.bytedata.detect.AllBCD;
import amidst.bytedata.detect.AnyBCD;
import amidst.bytedata.detect.ByteClassDetector;
import amidst.bytedata.detect.FieldFlagsBCD;
import amidst.bytedata.detect.LongBCD;
import amidst.bytedata.detect.NumberOfConstructorsBCD;
import amidst.bytedata.detect.NumberOfFieldsBCD;
import amidst.bytedata.detect.NumberOfMethodsAndConstructorsBCD;
import amidst.bytedata.detect.StringBCD;
import amidst.bytedata.detect.Utf8BCD;
import amidst.bytedata.detect.WildcardByteBCD;
import amidst.bytedata.prepare.ByteClassPreparer;
import amidst.bytedata.prepare.MethodBCP;
import amidst.bytedata.prepare.MultiBCP;
import amidst.bytedata.prepare.PropertyBCP;

public class BCFBuilder {
	public class BCDBuilder {
		private List<List<ByteClassDetector>> allDetectors = new ArrayList<List<ByteClassDetector>>();
		private List<ByteClassDetector> detectors = new ArrayList<ByteClassDetector>();

		private ByteClassDetector constructThis() {
			if (allDetectors.size() == 1) {
				return new AllBCD(allDetectors.get(0));
			} else {
				List<ByteClassDetector> result = new ArrayList<ByteClassDetector>();
				for (List<ByteClassDetector> detectors : allDetectors) {
					result.add(new AllBCD(detectors));
				}
				return new AnyBCD(result);
			}
		}

		public BCDBuilder or() {
			allDetectors.add(detectors);
			detectors = new ArrayList<ByteClassDetector>();
			return this;
		}

		public BCPBuilder prepare() {
			allDetectors.add(detectors);
			return BCFBuilder.this.preparerBuilder;
		}

		public BCDBuilder fieldFlags(int flags, int... fieldIndices) {
			detectors.add(new FieldFlagsBCD(flags, fieldIndices));
			return this;
		}

		public BCDBuilder longs(long... longs) {
			detectors.add(new LongBCD(longs));
			return this;
		}

		public BCDBuilder numberOfConstructors(int count) {
			detectors.add(new NumberOfConstructorsBCD(count));
			return this;
		}

		public BCDBuilder numberOfFields(int count) {
			detectors.add(new NumberOfFieldsBCD(count));
			return this;
		}

		public BCDBuilder numberOfMethodsAndConstructors(int count) {
			detectors.add(new NumberOfMethodsAndConstructorsBCD(count));
			return this;
		}

		public BCDBuilder strings(String... strings) {
			detectors.add(new StringBCD(strings));
			return this;
		}

		public BCDBuilder utf8s(String... utf8s) {
			detectors.add(new Utf8BCD(utf8s));
			return this;
		}

		public BCDBuilder wildcardBytes(int[] bytes) {
			detectors.add(new WildcardByteBCD(bytes));
			return this;
		}
	}

	public class BCPBuilder {
		private List<ByteClassPreparer> preparers = new ArrayList<ByteClassPreparer>();

		private ByteClassPreparer constructThis() {
			if (preparers.size() == 1) {
				return preparers.get(0);
			} else {
				return new MultiBCP(preparers);
			}
		}

		public BCFBuilder next() {
			return new BCFBuilder(BCFBuilder.this);
		}

		public List<ByteClassFinder> construct() {
			return BCFBuilder.this.construct();
		}

		public BCPBuilder addMethod(String method, String methodName) {
			preparers.add(new MethodBCP(method, methodName));
			return this;
		}

		public BCPBuilder addProperty(String property, String propertyName) {
			preparers.add(new PropertyBCP(property, propertyName));
			return this;
		}
	}

	public static BCFBuilder builder() {
		return new BCFBuilder(null);
	}

	private BCFBuilder previous;

	private String minecraftClassName;
	private BCDBuilder detectorBuilder = new BCDBuilder();
	private BCPBuilder preparerBuilder = new BCPBuilder();

	private BCFBuilder(BCFBuilder previous) {
		this.previous = previous;
	}

	public BCFBuilder name(String minecraftClassName) {
		this.minecraftClassName = minecraftClassName;
		return this;
	}

	public BCDBuilder detect() {
		return detectorBuilder;
	}

	public List<ByteClassFinder> construct() {
		List<ByteClassFinder> result;
		if (previous != null) {
			result = previous.construct();
		} else {
			result = new ArrayList<ByteClassFinder>();
		}
		result.add(constructThis());
		return result;
	}

	private ByteClassFinder constructThis() {
		Objects.requireNonNull(minecraftClassName,
				"a byte class finder needs to have a name");
		return new ByteClassFinder(minecraftClassName,
				detectorBuilder.constructThis(),
				preparerBuilder.constructThis());
	}
}
