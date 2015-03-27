package de.charite.compbio.exomiser.core.dao;

import com.google.common.collect.ImmutableList;

import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.TranscriptModel;

/**
 * Allows the easy creation of {@link JannovarData} objects for testing.
 * 
 * The generated data contains one transcript each for the genes FGFR2, GNRHR2A, RBM8A (overlaps with GNRHR2A), and SHH.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class TestJannovarDataFactory {

    public final ReferenceDictionary refDict;
    public final JannovarData jannovarData;

    public TestJannovarDataFactory() {
        this.refDict = HG19RefDictBuilder.build();
        this.jannovarData = buildJannovarData();
    }

    private JannovarData buildJannovarData() {
        final TestVariantFactory vfFactory = new TestVariantFactory();
        TranscriptModel tmFGFR2 = vfFactory.buildTMForFGFR2();
        TranscriptModel tmGNRHR2A = vfFactory.buildTMForGNRHR2A();
        TranscriptModel tmRBM8A = vfFactory.buildTMForRBM8A();
        TranscriptModel tmSHH = vfFactory.buildTMForSHH();
        return new JannovarData(refDict, ImmutableList.of(tmFGFR2, tmGNRHR2A, tmRBM8A, tmSHH));
    }

}