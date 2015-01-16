/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.jaiext.piecewise;

import it.geosolutions.jaiext.range.Range;
import it.geosolutions.jaiext.range.RangeFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenience class for linear transformations that maps an interval to another interval.
 * @author  Simone Giannecchini, GeoSolutions
 *
 *
 *
 * @source $URL$
 */
public class DefaultLinearPiecewiseTransform1DElement extends DefaultPiecewiseTransform1DElement implements
		PiecewiseTransform1DElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4026834241134908025L;

	private final static Logger LOGGER = Logger
			.getLogger(DefaultLinearPiecewiseTransform1DElement.class.toString());

	/**
     * @uml.property  name="outputMaximum"
     */
	private double outputMaximum;
	/**
     * @uml.property  name="outputMinimum"
     */
	private double outputMinimum;
	/**
     * @uml.property  name="outputRange"
     */
	private Range outputRange;
	/**
     * @uml.property  name="outputMinimumNaN"
     */
	private boolean outputMinimumNaN;
	/**
     * @uml.property  name="outputMaximumNaN"
     */
	private boolean outputMaximumNaN;
	/**
     * @uml.property  name="outputMinimumInfinite"
     */
	private boolean outputMinimumInfinite;
	/**
     * @uml.property  name="outputMaximumInfinite"
     */
	private boolean outputMaximumInfinite;



	/**
	 * Constructor.
	 * 
	 * @param name
	 *            for this {@link DefaultLinearPiecewiseTransform1DElement}.
	 * @param inRange
	 *            for this {@link DefaultLinearPiecewiseTransform1DElement}.
	 * @param outRange
	 *            for this {@link DefaultLinearPiecewiseTransform1DElement}.
	 */
	public DefaultLinearPiecewiseTransform1DElement(CharSequence name, Range inRange,
			Range outRange) {
		super(name, inRange);
		this.outputRange = RangeFactory.convertToDoubleRange(outRange);
		// /////////////////////////////////////////////////////////////////////
		//
		// Checks
		//
		// /////////////////////////////////////////////////////////////////////
		// //
		//
		// the output class can only be integer
		//
		// //
		final Class<? extends Number> type = outRange.getDataType().getClassValue();
		boolean minInc = outRange.isMinIncluded();
		boolean maxInc = outRange.isMaxIncluded();
		outputMinimum = PiecewiseUtilities.doubleValue(type, outRange
				.getMin(), minInc ? 0 : +1);
		outputMaximum = PiecewiseUtilities.doubleValue(type, outRange
				.getMax(), maxInc ? 0 : -1);
		outputMinimumNaN = Double.isNaN(outputMinimum);
		outputMaximumNaN = Double.isNaN(outputMaximum);
		outputMinimumInfinite = Double.isInfinite(outputMinimum);
		outputMaximumInfinite = Double.isInfinite(outputMaximum);

		// //
		//
		// No open intervals for the output range
		//
		// //
		if (outputMinimumInfinite || outputMaximumInfinite) {
			throw new IllegalArgumentException("Bad range defined");
		}

		final int compareOutBounds = PiecewiseUtilities.compare(outputMinimum,
				outputMaximum);
		// //
		//
		// the output values are correctly ordered
		//
		// //
		if (compareOutBounds > 0) {
			throw new IllegalArgumentException("Bad range defined");
		}

		// //
		//
		// mapping NaN to a single value
		//
		// //
		if (isInputMaximumNaN() && isInputMinimumNaN())
			if (compareOutBounds == 0) {
				setTransform(SingleDimensionTransformation.create(0, outputMinimum));
				setInverse(SingleDimensionTransformation.create(outputMinimum, 0));
				return;
			} else
				throw new IllegalArgumentException("Bad range defined");

		// //
		//
		// Mapping an open interval to a single value, there is no way to map an
		// open interval to another interval!
		//
		// //
		if (isInputMaximumInfinite() || isInputMinimumInfinite())
			if (compareOutBounds == 0) {
				setTransform(PiecewiseUtilities.createLinearTransform1D(0,
						outputMinimum));
				setInverse(null);
				return;
			} else
				throw new IllegalArgumentException("Bad range defined");

		final MathTransformation transform = PiecewiseUtilities.createLinearTransform1D(inRange,
				RangeFactory.create(outputMinimum, true, outputMaximum, true, true));
		setTransform(transform);

		// //
		//
		// Checking the created transformation
		//
		// //
		assert transform instanceof SingleDimensionTransformation;
		assert !Double.isNaN(((SingleDimensionTransformation) transform).getScale())
				&& !Double
						.isInfinite(((SingleDimensionTransformation) transform).getScale());

		// //
		//
		// Inverse
		//
		// //
		SingleDimensionTransformation tempTransform = (SingleDimensionTransformation) transform;
		final double scale = tempTransform.getScale();
		if (Math.abs(scale) < 1E-6)
			if (PiecewiseUtilities.compare(getInputMaximum(), getInputMinimum()) == 0)
				setInverse(SingleDimensionTransformation.create(0, getInputMinimum()));
			else
				setInverse(null);
		else
			setInverse((MathTransformation) transform.inverse());
	}

	/**
     * Returns the maximum output values for this  {@link DefaultLinearPiecewiseTransform1DElement}  ;
     * @return  the maximum output values for this  {@link DefaultLinearPiecewiseTransform1DElement}  ;
     * @uml.property  name="outputMaximum"
     */
	public double getOutputMaximum() {
		return outputMaximum;
	}

	/**
     * Returns the minimum output values for this  {@link DefaultLinearPiecewiseTransform1DElement}  ;
     * @return  the minimum output values for this  {@link DefaultLinearPiecewiseTransform1DElement}  ;
     * @uml.property  name="outputMinimum"
     */
	public double getOutputMinimum() {
		return outputMinimum;
	}

	/**
     * Returns the range for the output values for this {@link DefaultLinearPiecewiseTransform1DElement}  ;
     * @return  the range for the output values for this {@link DefaultLinearPiecewiseTransform1DElement}  ;
     * @uml.property  name="outputRange"
     */
	public Range getOutputRange() {
		return outputRange;
	}

	/**
     * Tells me if the lower boundary of the output range is NaN
     * @return  <code>true</code> if the lower boundary of the output range is  NaN, <code>false</code> otherwise.
     * @uml.property  name="outputMinimumNaN"
     */
	public boolean isOutputMinimumNaN() {
		return outputMinimumNaN;
	}

	/**
     * Tells me if the upper boundary of the output range is NaN
     * @return  <code>true</code> if the upper boundary of the output range is  NaN, <code>false</code> otherwise.
     * @uml.property  name="outputMaximumNaN"
     */
	public boolean isOutputMaximumNaN() {
		return outputMaximumNaN;
	}

	/**
     * Tells me if the lower boundary of the output range is infinite
     * @return  <code>true</code> if the lower boundary of the output range is  infinite, <code>false</code> otherwise.
     * @uml.property  name="outputMinimumInfinite"
     */
	public boolean isOutputMinimumInfinite() {
		return outputMinimumInfinite;
	}

	/**
     * Tells me if the upper boundary of the output range is infinite
     * @return  <code>true</code> if the upper boundary of the output range is  infinite, <code>false</code> otherwise.
     * @uml.property  name="outputMaximumInfinite"
     */
	public boolean isOutputMaximumInfinite() {
		return outputMaximumInfinite;
	}
	
	/**
	 * Retrieves the scale factor for this linear {@link PiecewiseTransform1DElement}.
	 * 
	 * @return the scale factor for this linear {@link PiecewiseTransform1DElement}.
	 */
	public double getScale(){
		//get the transform at this point it is linear for sure
		final SingleDimensionTransformation transform= (SingleDimensionTransformation) getTransform();
		return transform.getScale();
		
	}
	
	/**
	 * Retrieves the offset factor for this linear {@link PiecewiseTransform1DElement}.
	 * 
	 * @return the offset factor for this linear {@link PiecewiseTransform1DElement}.
	 */
	public double getOffset(){
		//get the transform at this point it is linear for sure
		final SingleDimensionTransformation transform= (SingleDimensionTransformation) getTransform();
		return transform.getOffset();
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.geotools.referencing.piecewise.DefaultPiecewiseTransform1DElement#toString()
	 */
	public String toString() {
		final StringBuilder buffer= new StringBuilder(super.toString());
		buffer.append("\n").append("output range=").append(this.outputRange);
		return buffer.toString();
	}

    protected Class<?> getEquivalenceClass(){
        return DefaultLinearPiecewiseTransform1DElement.class;
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj)
            return true;
        if(!(obj instanceof DefaultLinearPiecewiseTransform1DElement))
            return false;
        final DefaultLinearPiecewiseTransform1DElement that= (DefaultLinearPiecewiseTransform1DElement) obj;
        if(that.getEquivalenceClass()!=this.getEquivalenceClass())
            return false;
        if(!outputRange.equals(that.outputRange))
            return false;
        if(!PiecewiseUtilities.equals(outputMaximum, that.outputMaximum))
            return false;
        if(!PiecewiseUtilities.equals(outputMinimum, that.outputMinimum))
            return false;
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hashCode=37;
        hashCode=PiecewiseUtilities.hash(outputRange,hashCode);
        hashCode=PiecewiseUtilities.hash( outputMaximum,hashCode);
        hashCode=PiecewiseUtilities.hash( outputMinimum,hashCode);
        hashCode=PiecewiseUtilities.hash( super.hashCode(),hashCode);
        return hashCode;
    }
}
