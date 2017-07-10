package org.pentaho.di.trans.steps.shortmessage;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class ShortMessagePluginData extends BaseStepData implements StepDataInterface {
	public RowMetaInterface outputRowMeta;
	
	public int[] inStreamNrs;
	public String[] outStreamNrs;
	public int inputFieldNrs;

	public ShortMessagePluginData() {
		super();
	}
}
