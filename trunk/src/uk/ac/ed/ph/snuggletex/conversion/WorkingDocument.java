/* $Id: WorkingDocument.java,v 1.5 2008/04/22 09:37:43 dmckain Exp $
 *
 * Copyright 2008 University of Edinburgh.
 * All Rights Reserved
 */
package uk.ac.ed.ph.snuggletex.conversion;

import uk.ac.ed.ph.snuggletex.SnuggleLogicException;

import java.util.ArrayList;
import java.util.List;

/**
 * FIXME: Document this type!
 *
 * @author  David McKain
 * @version $Revision: 1.5 $
 */
public final class WorkingDocument {
    
    /**
     * Marker interface that can be applied to classes that provide information for a
     * {@link CharacterSource}.
     *
     * @author  David McKain
     * @version $Revision$
     */
    public static interface SourceContext {
        
        /* (Marker interface) */

    }
    
    /**
     * Represents a block of text that has been imported into this document.
     */
    public static final class CharacterSource {
        
        /** Contextual information about where this data came from */
        public final SourceContext context;
        
        /** 
         * The block that this block was initially substituted into, or null to indicate that it
         * is "top level" or was appended rather than substituted.
         */
        public final CharacterSource substitutedSource;
        
        /** 
         * The offset in {@link #substitutedSource} where the substitution occurred, or 0
         * if {@link #substitutedSource} is null.
         */
        public final int substitutionOffset;
        
        /** 
         * The text in the parent component that this data replaced, or null
         * if {@link #substitutedSource} is null.
         */
        public final CharSequence substitutedText;
        
        private transient String stringRepresentation;
        
        public CharacterSource(final SourceContext context) {
            this(context, null, 0, null);
        }
        
        public CharacterSource(final SourceContext context, final CharacterSource substitutedSource,
                final int substitutionOffset, final CharSequence substitutedText) {
            this.context = context;
            this.substitutedSource = substitutedSource;
            this.substitutionOffset = substitutionOffset;
            this.substitutedText = substitutedText;
        }

        @Override
        public String toString() {
            if (stringRepresentation==null) {
                StringBuilder resultBuilder = new StringBuilder(getClass().getSimpleName())
                    .append("(context=")
                    .append(context);
                if (substitutedSource!=null) {
                    resultBuilder.append(",substituted=").append(substitutedSource)
                        .append(",offset=").append(substitutionOffset)
                        .append(",substitutedText=").append(substitutedText);
                }
                resultBuilder.append(")");
                stringRepresentation = resultBuilder.toString();
            }
            return stringRepresentation;
        }
    }

    /**
     * FIXME: Document this type!
     */
    public static final class Slice {
        public final int startIndex;
        public final int endIndex;
        public final CharacterSource resolvedComponent;
        public final int componentIndexOffset;
        
        public Slice(final int startIndex, final int endIndex, final CharacterSource resolvedComponent, final int componentIndexOffset) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.resolvedComponent = resolvedComponent;
            this.componentIndexOffset = componentIndexOffset;
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName()
                + "(span=[" + startIndex + "," + endIndex + ") => " + resolvedComponent + "; offset " + componentIndexOffset + ")";
        }
    }
    
    /**
     * FIXME: Document this type!
     *
     * @author  David McKain
     * @version $Revision: 1.5 $
     */
    public static class IndexResolution {
        public final int scoreboardIndex;
        public final Slice slice;
        public final int indexInComponent;
        
        public IndexResolution(final int scoreboardIndex, final Slice slice, final int indexInComponent) {
            this.scoreboardIndex = scoreboardIndex;
            this.slice = slice;
            this.indexInComponent = indexInComponent;
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName()
                + "(sbIndex=" + scoreboardIndex
                + ", slice=" + slice
                + ", indexInComponent=" + indexInComponent
                + ")";
        }
    }
    
    //----------------------------------------------------------------
    
    private final StringBuilder buffer;
    private final List<Slice> scoreBoard;

    /** Current length of document (kept synced with buffer) */
    private int length;
    
    private int freezeIndex;
    
    public WorkingDocument(final CharSequence initialData, final SourceContext context) {
        this.scoreBoard = new ArrayList<Slice>();
        this.freezeIndex = 0;
        
        /* Import data into buffer */
        this.buffer = new StringBuilder(initialData);
        
        /* Set current length based on the length of buffer */
        this.length = buffer.length();
        
        /* Set up scoreboard to contain the initial data only */
        CharacterSource initialComponent = new CharacterSource(context);
        scoreBoard.add(new Slice(0, length, initialComponent, 0));
    }
    
    public FrozenSlice freezeSlice(final int startIndex, final int endIndex) {
        checkRange(startIndex, endIndex);
        freezeIndex = Math.max(freezeIndex, endIndex);
        return new FrozenSlice(this, startIndex, endIndex);
    }
    
    /**
     * This is a rather nasty method which resets the "freeze index" of this document back to the
     * given index. Any {@link FrozenSlice}s that may previously have spanned this index are no
     * longer valid as their contents may change.
     * <p>
     * This class does NOT attempt to enforce this in any way - it is up to the caller to make
     * sure he/she understands the implications of doing this.
     * <p>
     * The main requirement for having this is when evaluating single-argument user-defined commands
     * like <tt>\\udc \\alpha</tt> where the argument is implicitly taken to be the next
     * valid token.
     * 
     * @param newFreezeIndex
     */
    public void unfreeze(final int newFreezeIndex) {
        this.freezeIndex = newFreezeIndex;
    }
    
    public int length() {
        return length;
    }
    
    /** Index in scoreboard of last successful resolution used in {@link #resolveIndex(int, boolean)} below */
    int lastResolvedSliceIndex = 0;
    
    public IndexResolution resolveIndex(final int index, final boolean fallLeftOnBoundaries) {
        checkIndex(index, "Index");
        if (fallLeftOnBoundaries && index==0) {
            /* If falling to left at index 0, then we miss the first component completely. This is
             * not useful in practice!
             */
            return null;
        }
        
        /* Search for the slice containing this index. We'll search from the index of the last
         * successful call to here (0 on first call) since most calls to this method are made on
         * indices quite close together.
         */
        IndexResolution result = null;
        Slice mapping;
        int sliceIndex = lastResolvedSliceIndex;
        int numSlices = scoreBoard.size();
        while (sliceIndex>=0 && sliceIndex<numSlices) {
            mapping = scoreBoard.get(sliceIndex);
            if (mapping.startIndex>index) {
            	/* We're too far to the right, so let's go back one place. */
            	sliceIndex--;
            }
            else if (index<mapping.endIndex || (fallLeftOnBoundaries && index==mapping.endIndex)) {
            	/* Success! */
                result = new IndexResolution(sliceIndex, mapping, index + mapping.componentIndexOffset);
                break;
            }
            else {
            	/* Move to the right */
            	sliceIndex++;
            }
        }
        lastResolvedSliceIndex = result!=null ? sliceIndex : lastResolvedSliceIndex;
        return result;
    }
    
    public int charAt(final int index) {
        if (index>=0 && index<length) {
            return buffer.charAt(index);
        }
        return -1;
    }
    
    public int indexOf(final int startSearchIndex, final char c) {
        for (int i=startSearchIndex; i<length(); i++) {
            if (charAt(i)==c) {
                return i;
            }
        }
        return -1;
    }

    public boolean matchesAt(final int index, final char c) {
        return charAt(index)==c;
    }

    public boolean matchesAt(final int index, final String s) {
        if (length()-index < s.length()) {
            /* Can't possibly match if we don't have enough characters */
            return false;
        }
        return s.equals(extract(index, index+s.length()));
    }
    
    public boolean isRegionWhitespace(final int startIndex, final int endIndex) {
        if (startIndex==endIndex) {
            return false;
        }
        for (int i=startIndex; i<endIndex; i++) {
            if (!Character.isWhitespace(charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public CharSequence extract() {
        return buffer;
    }
    
    public CharSequence extract(final int startIndex, final int endIndex) {
        checkRange(startIndex, endIndex);
        return buffer.subSequence(startIndex, endIndex);
    }
    
    public CharacterSource substitute(final int startIndex, final int endIndex,
            final CharSequence data, final SourceContext context) {
        checkRange(startIndex, endIndex);
        
        /* Make sure we are not trying to change the frozen part of the document */
        if (startIndex < freezeIndex) {
            throw new IllegalArgumentException("Cannot modify frozen part of document (startIndex=" + startIndex
                    + ",freezeIndex=" + freezeIndex
                    + ",attemptedText=" + buffer.substring(freezeIndex, Math.min(freezeIndex+20, length))
                    + ")");
        }
        
        /* Work out which components the start of the substitution lies in */
        IndexResolution startResolution = resolveIndex(startIndex, false);
        
        if (startResolution==null) {
            /* Special case - we are *appending* to the document, rather than substituting
             * existing content. This case is somewhat simpler to deal with so we will do it
             * all now.
             */
            CharacterSource toAppend = new CharacterSource(context);
            int newStartIndex = length;
            buffer.append(data);
            int newLength = buffer.length();
            Slice newEndSlice = new Slice(newStartIndex, newLength, toAppend, -newStartIndex);
            scoreBoard.add(newEndSlice);
            length = newLength;
            return toAppend;
        }
        
        /* If still here, then we're doing a genuine substitution (or insertion). The first
         * thing we do is modify the StringBuilder.
         */
        CharSequence beingReplaced = buffer.subSequence(startIndex, endIndex);
        buffer.delete(startIndex, endIndex);
        buffer.insert(startIndex, data);

        /* Next up, we have to rebuild the scoreboard. This is much more complicated so is best
         * demonstrated with some examples.
         * 
         * First note that the scoreboard will change from the slice containing to the startIndex
         * all the way to the end.
         * 
         * Let's assume for convenience that there are 4 entries in the scoreboard and that
         * the resolved startIndex lies within the entry with index 1 (i.e. the second entry).
         * 
         * [..............)[...............)[..................)[..............)
         * 
         * Expl 1:         [XXXXXXXXX) is being replaced with [YYYYYYY) to give
         * [..............)[YYYYYYY)[...)[..................)[..............)
         *     This stays     repl  left-   next component         similarly
         *     the same             over    has offset shifted
         *     
         * Here the replacement's left boundary sits nicely against an existing slice.
         *     
         * Expl 2:           [XXXXXXXXX) is being replaced with [YYYYYYY) to give
         * [..............)[)[YYYYYYY)[.)[..................)[..............)
         *   Stays same    cut repl   left   shift                shift
         *   
         * Here we need to create a slice out of the second component before continuing
         * in the same way.
         *   
         * Expl 3:         [XXXXXXXXXXXXXXXXXXXXX) is being replaced with [Y) to give
         * [..............)[Y)[..........)[..............)
         *   Stays same    repl  slice       shift left
         *   
         * Here, the replacement spans more than 1 slice. The original 3rd slice needs
         * to be cut down and shifted left. The 4th slice is just shifted left. Phew!!!
         */
        
        /* Anyway, in all cases we start off by keeping the slices that lie before the
         * one containing the start of the substitution.
         */
        int startSliceIndex = startResolution.scoreboardIndex;
        List<Slice> newScoreBoard = new ArrayList<Slice>();
        for (int i=0; i<startSliceIndex; i++) {
            newScoreBoard.add(scoreBoard.get(i));
        }
        
        /* Next up, we look at the slice containing the start of the substitution, making a
         * new slice to reflect "the bit before" if it is required. The offset of this component
         * does not change.
         */
        Slice startSlice = startResolution.slice;
        if (startIndex > startSlice.startIndex) {
            Slice bitBefore = new Slice(startSlice.startIndex, startIndex,
                    startSlice.resolvedComponent, startSlice.componentIndexOffset);
            newScoreBoard.add(bitBefore);
        }
        
        /* Now we create a slice corresponding to the substitution. The offset of this slice is
         * set to ensure that currentIndex corresponds to index 0 in the substitution - i.e.
         * shift left by currentIndex.
         */
        int currentIndex = startIndex;
        int substitutionSize = data.length();
        CharacterSource result = new CharacterSource(context, startResolution.slice.resolvedComponent,
                startResolution.indexInComponent, beingReplaced);
        Slice substitutionSlice = new Slice(currentIndex, currentIndex + substitutionSize, result, -currentIndex);
        currentIndex += substitutionSize;
        newScoreBoard.add(substitutionSlice);
        
        /* Next we need to find out where the end of the substitution is. All slices before this
         * one will be removed from the scoreboard. From the current end slice, we will re-slice
         * it if required to include whatever comes immediately after the substitution end point.
         * 
         * To calculate what happens to the offset, we look at 'endIndex' (before) and
         * 'currentIndex' (after). Both must map to the same point in the original character source.
         * Thus:
         * 
         * endIndex + before offset comp = currentIndex + after offset
         * 
         * i.e. after offset = before offset + before index in comp + endIndex - currentIndex;
         */
        IndexResolution endResolution = resolveIndex(endIndex, true);
        Slice endSlice = endResolution.slice;
        if (endIndex < endSlice.endIndex) {
            int bitAfterSize = endSlice.endIndex - endIndex;
            int resultOffset = endSlice.componentIndexOffset + endIndex - currentIndex;
            Slice bitAfter = new Slice(currentIndex, currentIndex + bitAfterSize,
                    endSlice.resolvedComponent, resultOffset);
            newScoreBoard.add(bitAfter);
            currentIndex += bitAfterSize;
        }
        
        /* Finally, we include shifted versions of all slices that followed the end index in the
         * original scoreboard. The change in offset is calculated by looking at the left
         * endpoint of the slice before and after shifted, which must map to the same point
         * in the original character source. Thus we have:
         * 
         * before startIndex + before offset = after startIndex + after offset
         * 
         * i.e. after offset = before offset + before startIndex - after startIndex
         */
        Slice trailingSlice, shiftedTrailingSlice;
        for (int i=endResolution.scoreboardIndex+1, size=scoreBoard.size(); i<size; i++) {
            trailingSlice = scoreBoard.get(i);
            int afterSliceLength = trailingSlice.endIndex - trailingSlice.startIndex;
            int shiftedOffset = trailingSlice.componentIndexOffset + trailingSlice.startIndex - currentIndex;
            shiftedTrailingSlice = new Slice(currentIndex, currentIndex + afterSliceLength,
                    trailingSlice.resolvedComponent, shiftedOffset);
            newScoreBoard.add(shiftedTrailingSlice);
            currentIndex += afterSliceLength;
        }
        
        /* Replace scoreboard */
        scoreBoard.clear();
        scoreBoard.addAll(newScoreBoard);
        
        /* Finally sync up lengths */
        if (buffer.length()!=currentIndex) {
            throw new SnuggleLogicException("Failed sanity check: buffer length is " + buffer.length() + ", last board index=" + currentIndex);
        }
        length = currentIndex;
        return result;
    }
    
    private void checkRange(final int startIndex, final int endIndex) {
        checkIndex(startIndex, "Start Index");
        checkIndex(endIndex, "End Index");
        if (startIndex>endIndex) {
            throw new IllegalArgumentException("Start index " + startIndex + " must be <= end index " + endIndex);
        }
    }
    
    private void checkIndex(final int index, final String errorStart) {
        if (!(index>=0 && index<=length)) {
            throw new IndexOutOfBoundsException(errorStart + " " + index
                    + " is outwith the current bounds [0," + length + ")");
        }
    }
    
    //---------------------------------------------------
    // For debugging during development
    
    public void dumpScoreboard() {
        for (Slice mapping : scoreBoard) {
            System.out.println(mapping);
        }
    }
    
    public static void main(String[] args) {
        SourceContext ctx = null;
        WorkingDocument d = new WorkingDocument("\\mycommand blah blah", ctx);
      
        System.out.println("INITIAL STATE\n");
//        d.dumpScoreboard();
//        
        d.substitute(0, 10, "This expands to \\bob", ctx);
        System.out.println("AFTER FIRST");
        d.dumpScoreboard();
//        
        d.substitute(16, 20, "[Bob expanded]", ctx);
        System.out.println("\nAFTER SECOND");
        d.dumpScoreboard();
      
//        d.substitute(d.length(), d.length(), "You", ctx);
//      
//        System.out.println("\nAFTER FIRST SUBS\n");
//        d.dumpScoreboard();        
//        
//        d.substitute(1,1, "INSERTED", ctx);
//        d.substitute(14,15, "a", ctx);
//        d.substitute(2,3, "stuff", ctx);
////        
//        System.out.println("\nAFTER SECOND SUBS\n");
//        d.dumpScoreboard();        
//        System.out.println("STATE IS NOW " + d.extract());
        
//        d.freezeSlice(0, 10);
//        d.substitute(10, 12, "bob", ctx);
        
        System.out.println(d.extract());
    }
}


