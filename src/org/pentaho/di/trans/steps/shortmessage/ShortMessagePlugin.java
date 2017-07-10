package org.pentaho.di.trans.steps.shortmessage;

import java.util.UUID;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.mascloud.sdkclient.Client;

public class ShortMessagePlugin extends BaseStep implements StepInterface {
	private static Class<?> PKG = ShortMessagePlugin.class;
	//静态连接
	static Client client = Client.getInstance();

	private ShortMessagePluginData data;
	private ShortMessagePluginMeta meta;

	public ShortMessagePlugin(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
			Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private Object[] getOneRow(RowMetaInterface rowMeta, Object[] row) throws KettleException {
		Object[] outData = new Object[data.outputRowMeta.size()];
		// 复制输入流的信息，一行
		System.arraycopy(row, 0, outData, 0, rowMeta.size());
		int length = meta.getFieldInStream().length;
		String cellNo = null;
		String message = null;
		for (int i = 0; i < length; i++) {
			String valueIn = getInputRowMeta().getString(row, data.inStreamNrs[i]);
			if (i == 0) {
				cellNo = valueIn.trim();
			}
			if (i == 1) {
				message = valueIn.trim();
			}
		}
		int result = sendMessage(cellNo, message);
		outData[2] = result;
		return outData;
	}

	@Override
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		this.meta = (ShortMessagePluginMeta) smi;
		this.data = (ShortMessagePluginData) sdi;

		// 获取上一个步骤输入数据的一行
		Object[] rowData = getRow();
		if (rowData == null) {
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;

			data.outputRowMeta = getInputRowMeta().clone();
			data.inputFieldNrs = data.outputRowMeta.size();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this, repository, metaStore);

			data.inStreamNrs = new int[meta.getFieldInStream().length];
			for (int i = 0; i < meta.getFieldInStream().length; i++) {
				data.inStreamNrs[i] = getInputRowMeta().indexOfValue(meta.getFieldInStream()[i]);
				if (data.inStreamNrs[i] < 0) {
					throw new KettleStepException(
							BaseMessages.getString(PKG, "SMS.Exception.FieldRequired", meta.getFieldInStream()[i]));
				}
				if (getInputRowMeta().getValueMeta(data.inStreamNrs[i]).getType() != ValueMetaInterface.TYPE_STRING) {
					throw new KettleStepException(BaseMessages.getString(PKG, "SMS.Exception,FieldTypeNotString",
							meta.getFieldInStream()[i]));
				}
			}
			data.outStreamNrs = new String[meta.getFieldInStream().length];
			for (int i = 0; i < meta.getFieldInStream().length; i++) {
				data.outStreamNrs[i] = environmentSubstitute(meta.getFieldOutStream()[i]);
			}
		}

		Object[] outData = getOneRow(getInputRowMeta(), rowData);
		// 新的输出行，用于给下个步骤使用
		try {
			putRow(data.outputRowMeta, outData);

			if (checkFeedback(getLinesRead())) {
				if (log.isDetailed()) {
					logDetailed(BaseMessages.getString(PKG, "SMS.Log.LineNumber") + getLinesRead());
				}
			}
		} catch (KettleException e) {
			boolean sendToErrorRow = false;
			String errorMessage = null;

			if (getStepMeta().isDoingErrorHandling()) {
				sendToErrorRow = true;
				errorMessage = e.toString();
			} else {
				logError(BaseMessages.getString(PKG, "SMS.Log.ErrorInStep", e.getMessage()));
				setErrors(1);
				stopAll();
				setOutputDone();
				return false;
			}
			if (sendToErrorRow) {
				putError(getInputRowMeta(), rowData, 1, errorMessage, null, "SMS001");
			}
		}
		return true;
	}

	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		this.meta = (ShortMessagePluginMeta) smi;
		this.data = (ShortMessagePluginData) sdi;

		// 将登录信息在init函数中运行，该函数在转换开始时调用，返回的结果是boolean类型
		if (super.init(smi, sdi)) {
			String url = meta.getUrl().trim();
			String userAccount = meta.getUserAccount().trim();
			String password = meta.getPassword().trim();
			String ecname = meta.getEcname().trim();
			
			boolean loginResult = client.login(url, userAccount, password, ecname);
			System.out.println(loginResult);
			return loginResult;
		}
		return super.init(smi, sdi);
	}

	public int sendMessage(String cellNo, String message) {
		String autograph = meta.getAutograph().trim();
		System.out.println(autograph);
		int sendResult = client.sendDSMS(new String[] { cellNo }, message, "", 1, autograph,
				UUID.randomUUID().toString(), true);
		System.out.println(sendResult);
		return sendResult;
	}

	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		this.meta = (ShortMessagePluginMeta) smi;
		this.data = (ShortMessagePluginData) sdi;

		super.dispose(smi, sdi);
	}
}
