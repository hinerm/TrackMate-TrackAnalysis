package fiji.plugin.trackmate.features.track;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.ImageIcon;

import net.imglib2.multithreading.SimpleMultiThreading;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Dimension;
import fiji.plugin.trackmate.FeatureModel;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.features.edge.LinearTrackEdgeStatistics;

@SuppressWarnings( "deprecation" )
@Plugin( type = TrackAnalyzer.class, priority = Priority.LOW )
public class LinearTrackDescriptor implements TrackAnalyzer
{

	public static final String KEY = "Linear track analysis";

	public static final String TRACK_TOTAL_DISTANCE_TRAVELED = "TOTAL_DISTANCE_TRAVELED";

	public static final String TRACK_MAX_DISTANCE_TRAVELED = "MAX_DISTANCE_TRAVELED";

	public static final String TRACK_CONFINMENT_RATIO = "CONFINMENT_RATIO";

	public static final String TRACK_MEAN_STRAIGHT_LINE_SPEED = "MEAN_STRAIGHT_LINE_SPEED";

	public static final String TRACK_LINEARITY_OF_FORWARD_PROGRESSION = "LINEARITY_OF_FORWARD_PROGRESSION";

	public static final String TRACK_MEAN_DIRECTIONAL_CHANGE_RATE = "MEAN_DIRECTIONAL_CHANGE_RATE";

	public static final String TOTAL_ABSOLUTE_ANGLE_XY = "TOTAL_ABSOLUTE_ANGLE_XY";

	public static final String TOTAL_ABSOLUTE_ANGLE_YZ = "TOTAL_ABSOLUTE_ANGLE_YZ";

	public static final String TOTAL_ABSOLUTE_ANGLE_ZX = "TOTAL_ABSOLUTE_ANGLE_ZX";

	public static final List< String > FEATURES = new ArrayList<>( 9 );

	public static final Map< String, String > FEATURE_NAMES = new HashMap<>( 9 );

	public static final Map< String, String > FEATURE_SHORT_NAMES = new HashMap<>( 9 );

	public static final Map< String, Dimension > FEATURE_DIMENSIONS = new HashMap<>( 9 );

	public static final Map< String, Boolean > IS_INT = new HashMap<>( 9 );

	static
	{
		FEATURES.add( TRACK_TOTAL_DISTANCE_TRAVELED );
		FEATURES.add( TRACK_MAX_DISTANCE_TRAVELED );
		FEATURES.add( TRACK_CONFINMENT_RATIO );
		FEATURES.add( TRACK_MEAN_STRAIGHT_LINE_SPEED );
		FEATURES.add( TRACK_LINEARITY_OF_FORWARD_PROGRESSION );
		FEATURES.add( TRACK_MEAN_DIRECTIONAL_CHANGE_RATE );
		FEATURES.add( TOTAL_ABSOLUTE_ANGLE_XY );
		FEATURES.add( TOTAL_ABSOLUTE_ANGLE_YZ );
		FEATURES.add( TOTAL_ABSOLUTE_ANGLE_ZX );

		FEATURE_NAMES.put( TRACK_TOTAL_DISTANCE_TRAVELED, "Total distance traveled" );
		FEATURE_NAMES.put( TRACK_MAX_DISTANCE_TRAVELED, "Max distance traveled" );
		FEATURE_NAMES.put( TRACK_CONFINMENT_RATIO, "Confinment ratio" );
		FEATURE_NAMES.put( TRACK_MEAN_STRAIGHT_LINE_SPEED, "Mean straight line speed" );
		FEATURE_NAMES.put( TRACK_LINEARITY_OF_FORWARD_PROGRESSION, "Linearity of forward progression" );
		FEATURE_NAMES.put( TRACK_MEAN_DIRECTIONAL_CHANGE_RATE, "Mean directional change rate" );
		FEATURE_NAMES.put( TOTAL_ABSOLUTE_ANGLE_XY, "Absolute angle in xy plane" );
		FEATURE_NAMES.put( TOTAL_ABSOLUTE_ANGLE_YZ, "Absolute angle in yz plane" );
		FEATURE_NAMES.put( TOTAL_ABSOLUTE_ANGLE_ZX, "Absolute angle in zx plane" );

		FEATURE_SHORT_NAMES.put( TRACK_TOTAL_DISTANCE_TRAVELED, "Total dist." );
		FEATURE_SHORT_NAMES.put( TRACK_MAX_DISTANCE_TRAVELED, "Max dist." );
		FEATURE_SHORT_NAMES.put( TRACK_CONFINMENT_RATIO, "Cnfnmnt ratio" );
		FEATURE_SHORT_NAMES.put( TRACK_MEAN_STRAIGHT_LINE_SPEED, "Mean v. line" );
		FEATURE_SHORT_NAMES.put( TRACK_LINEARITY_OF_FORWARD_PROGRESSION, "Lin. fwd. progr." );
		FEATURE_SHORT_NAMES.put( TRACK_MEAN_DIRECTIONAL_CHANGE_RATE, "Mean 𝛾 rate" );
		FEATURE_SHORT_NAMES.put( TOTAL_ABSOLUTE_ANGLE_XY, "Abs. angle xy" );
		FEATURE_SHORT_NAMES.put( TOTAL_ABSOLUTE_ANGLE_YZ, "Abs. angle yz" );
		FEATURE_SHORT_NAMES.put( TOTAL_ABSOLUTE_ANGLE_ZX, "Abs. angle zx" );

		FEATURE_DIMENSIONS.put( TRACK_TOTAL_DISTANCE_TRAVELED, Dimension.LENGTH );
		FEATURE_DIMENSIONS.put( TRACK_MAX_DISTANCE_TRAVELED, Dimension.LENGTH );
		FEATURE_DIMENSIONS.put( TRACK_CONFINMENT_RATIO, Dimension.NONE );
		FEATURE_DIMENSIONS.put( TRACK_MEAN_STRAIGHT_LINE_SPEED, Dimension.VELOCITY );
		FEATURE_DIMENSIONS.put( TRACK_LINEARITY_OF_FORWARD_PROGRESSION, Dimension.NONE );
		FEATURE_DIMENSIONS.put( TRACK_MEAN_DIRECTIONAL_CHANGE_RATE, Dimension.RATE );
		FEATURE_DIMENSIONS.put( TOTAL_ABSOLUTE_ANGLE_XY, Dimension.ANGLE );
		FEATURE_DIMENSIONS.put( TOTAL_ABSOLUTE_ANGLE_YZ, Dimension.ANGLE );
		FEATURE_DIMENSIONS.put( TOTAL_ABSOLUTE_ANGLE_ZX, Dimension.ANGLE );

		IS_INT.put( TRACK_TOTAL_DISTANCE_TRAVELED, Boolean.FALSE );
		IS_INT.put( TRACK_MAX_DISTANCE_TRAVELED, Boolean.FALSE );
		IS_INT.put( TRACK_CONFINMENT_RATIO, Boolean.FALSE );
		IS_INT.put( TRACK_MEAN_STRAIGHT_LINE_SPEED, Boolean.FALSE );
		IS_INT.put( TRACK_LINEARITY_OF_FORWARD_PROGRESSION, Boolean.FALSE );
		IS_INT.put( TRACK_MEAN_DIRECTIONAL_CHANGE_RATE, Boolean.FALSE );
		IS_INT.put( TOTAL_ABSOLUTE_ANGLE_XY, Boolean.FALSE );
		IS_INT.put( TOTAL_ABSOLUTE_ANGLE_YZ, Boolean.FALSE );
		IS_INT.put( TOTAL_ABSOLUTE_ANGLE_ZX, Boolean.FALSE );
	}

	private int numThreads;

	private long processingTime;

	public LinearTrackDescriptor()
	{
		setNumThreads();
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	@Override
	public Map< String, Dimension > getFeatureDimensions()
	{
		return FEATURE_DIMENSIONS;
	}

	@Override
	public Map< String, String > getFeatureNames()
	{
		return FEATURE_NAMES;
	}

	@Override
	public Map< String, String > getFeatureShortNames()
	{
		return FEATURE_SHORT_NAMES;
	}

	@Override
	public List< String > getFeatures()
	{
		return FEATURES;
	}

	@Override
	public Map< String, Boolean > getIsIntFeature()
	{
		return IS_INT;
	}

	@Override
	public boolean isManualFeature()
	{
		return false;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getInfoText()
	{
		return null;
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public String getName()
	{
		return KEY;
	}

	@Override
	public int getNumThreads()
	{
		return numThreads;
	}

	@Override
	public void setNumThreads()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = numThreads;

	}

	@Override
	public boolean isLocal()
	{
		return true;
	}

	@Override
	public void process( final Collection< Integer > trackIDs, final Model model )
	{

		if ( trackIDs.isEmpty() )
		{ return; }

		final ArrayBlockingQueue< Integer > queue = new ArrayBlockingQueue<>( trackIDs.size(), false, trackIDs );
		final FeatureModel fm = model.getFeatureModel();

		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			threads[ i ] = new Thread( KEY + " thread " + i )
			{
				@Override
				public void run()
				{
					Integer trackID;
					while ( ( trackID = queue.poll() ) != null )
					{
						/*
						 * Get the first spot (lowest FRAME).
						 */

						final List< Spot > spots = new ArrayList<>( model.getTrackModel().trackSpots( trackID ) );
						Collections.sort( spots, Spot.frameComparator );
						final Spot first = spots.get( 0 );

						/*
						 * Iterate over edges.
						 */

						final Set< DefaultWeightedEdge > edges = model.getTrackModel().trackEdges( trackID );

						double totalDistance = 0.;
						double maxDistanceSq = Double.NEGATIVE_INFINITY;
						double maxDistance = 0.;
						double sumAngleSpeed = 0.;
						int nAngleSpeed = 0;
						double dx = 0;
						double dy = 0;
						double dz = 0;

						for ( final DefaultWeightedEdge edge : edges )
						{
							// Total distance travelled.
							final Spot source = model.getTrackModel().getEdgeSource( edge );
							final Spot target = model.getTrackModel().getEdgeTarget( edge );
							final double d = Math.sqrt( source.squareDistanceTo( target ) );
							totalDistance += d;

							// Max distance traveled.
							final double dToFirstSq = first.squareDistanceTo( target );
							if ( dToFirstSq > maxDistanceSq )
							{
								maxDistanceSq = dToFirstSq;
								maxDistance = Math.sqrt( maxDistanceSq );
							}

							/*
							 * Mean rate of directional change. We depend on the
							 * edge feature
							 */

							final Double val = fm.getEdgeFeature( edge, LinearTrackEdgeStatistics.DIRECTIONAL_CHANGE_RATE );
							if ( null != val && !val.isNaN() )
							{
								sumAngleSpeed += val.doubleValue();
								nAngleSpeed++;
							}

							dx += target.getDoublePosition( 0 ) - source.getDoublePosition( 0 );
							dy += target.getDoublePosition( 1 ) - source.getDoublePosition( 1 );
							dz += target.getDoublePosition( 2 ) - source.getDoublePosition( 2 );
						}

						/*
						 * Compute features.
						 */

						// Dependency features.
						final double netDistance = fm.getTrackFeature( trackID, TrackDurationAnalyzer.TRACK_DISPLACEMENT );
						final double tTotal = fm.getTrackFeature( trackID, TrackDurationAnalyzer.TRACK_DURATION );
						final double vMean = fm.getTrackFeature( trackID, TrackSpeedStatisticsAnalyzer.TRACK_MEAN_SPEED );

						// Our features.
						final double confinmentRatio = netDistance / totalDistance;
						final double meanStraightLineSpeed = netDistance / tTotal;
						final double linearityForwardProgression = meanStraightLineSpeed / vMean;
						final double meanAngleSpeed = sumAngleSpeed / nAngleSpeed;

						// Angle features.
						final double angleXY = Math.atan2( dy, dx );
						final double angleYZ = Math.atan2( dz, dy );
						final double angleZX = Math.atan2( dx, dz );

						// Store.
						fm.putTrackFeature( trackID, TRACK_TOTAL_DISTANCE_TRAVELED, totalDistance );
						fm.putTrackFeature( trackID, TRACK_MAX_DISTANCE_TRAVELED, maxDistance );
						fm.putTrackFeature( trackID, TRACK_CONFINMENT_RATIO, confinmentRatio );
						fm.putTrackFeature( trackID, TRACK_MEAN_STRAIGHT_LINE_SPEED, meanStraightLineSpeed );
						fm.putTrackFeature( trackID, TRACK_LINEARITY_OF_FORWARD_PROGRESSION, linearityForwardProgression );
						fm.putTrackFeature( trackID, TRACK_MEAN_DIRECTIONAL_CHANGE_RATE, meanAngleSpeed );
						fm.putTrackFeature( trackID, TOTAL_ABSOLUTE_ANGLE_XY, angleXY );
						fm.putTrackFeature( trackID, TOTAL_ABSOLUTE_ANGLE_YZ, angleYZ );
						fm.putTrackFeature( trackID, TOTAL_ABSOLUTE_ANGLE_ZX, angleZX );
					}
				}
			};
		}

		final long start = System.currentTimeMillis();
		SimpleMultiThreading.startAndJoin( threads );
		final long end = System.currentTimeMillis();
		processingTime = end - start;
	}
}
