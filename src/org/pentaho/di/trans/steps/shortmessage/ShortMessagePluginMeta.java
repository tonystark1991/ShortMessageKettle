package org.pentaho.di.trans.steps.shortmessage;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.trans.steps.shortmessage.ShortMessagePluginDialog;
import org.w3c.dom.Node;
import org.pentaho.di.repository.Repository;

import org.pentaho.metastore.api.IMetaStore;

public class ShortMessagePluginMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = ShortMessagePluginMeta.class;

	// dialog类输入用户信息做一个连接池
	private String url;// 身份认证地址
	private String userAccount;// 用户登录账号
	private String password;// 用户登录密码
	private String ecname;// 用户企业名称
	private String autograph;// 短信签名

	// 获取输入的字段信息，并赋予输出的字段信息
	private String[] fieldInStream;
	private String[] fieldOutStream;

	public String[] getFieldInStream() {
		return fieldInStream;
	}

	public void setFieldInStream(String[] fieldInStream) {
		this.fieldInStream = fieldInStream;
	}

	public String[] getFieldOutStream() {
		return fieldOutStream;
	}

	public void setFieldOutStream(String[] fieldOutStream) {
		this.fieldOutStream = fieldOutStream;
	}

	public String getAutograph() {
		return autograph;
	}

	public void setAutograph(String autograph) {
		this.autograph = autograph;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEcname() {
		return ecname;
	}

	public void setEcname(String ecname) {
		this.ecname = ecname;
	}

	// 构造器
	public ShortMessagePluginMeta() {
		super();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleXMLException {
		readData(stepnode);
	}

	public void allocate(int nrkeys) {
		fieldInStream = new String[nrkeys];
		fieldOutStream = new String[nrkeys];
	}

	public Object clone() {
		ShortMessagePluginMeta retval = (ShortMessagePluginMeta) super.clone();
		int nrkeys = fieldInStream.length;

		retval.allocate(nrkeys);
		System.arraycopy(fieldInStream, 0, retval.fieldInStream, 0, nrkeys);
		System.arraycopy(fieldOutStream, 0, retval.fieldOutStream, 0, nrkeys);
		return retval;
	}

	public void readData(Node stepnode) {

		url = XMLHandler.getTagValue(stepnode, "url");
		userAccount = XMLHandler.getTagValue(stepnode, "userAccount");
		password = XMLHandler.getTagValue(stepnode, "password");
		ecname = XMLHandler.getTagValue(stepnode, "ecname");
		autograph = XMLHandler.getTagValue(stepnode, "autograph");
		int nrkeys;

		Node lookup = XMLHandler.getSubNode(stepnode, "fields");
		nrkeys = XMLHandler.countNodes(lookup, "field");

		allocate(nrkeys);

		for (int i = 0; i < nrkeys; i++) {
			Node fnode = XMLHandler.getSubNodeByNr(lookup, "field", i);
			fieldInStream[i] = Const.NVL(XMLHandler.getTagValue(fnode, "fieldInStream"), "");
			fieldOutStream[i] = Const.NVL(XMLHandler.getTagValue(fnode, "fieldOutStream"), "");
		}
	}

	@Override
	public void setDefault() {

		fieldInStream = null;
		fieldOutStream = null;

		int nrkeys = 0;

		allocate(nrkeys);
	}

	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
			VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException {
		for (int i = 0; i < fieldOutStream.length; i++) {
			ValueMetaInterface v;
			if (!Utils.isEmpty(fieldOutStream[i])) {
				v = new ValueMetaString(space.environmentSubstitute(fieldOutStream[i]));
				v.setLength(100, -1);
				v.setOrigin(name);
				inputRowMeta.addValueMeta(v);
			} else {
				v = inputRowMeta.searchValueMeta(fieldInStream[i]);
				if (v == null) {
					continue;
				}
				v.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
			}
		}
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer();

		// 首先将client连接所需要的数据进行XML文件的拼接
		retval.append("    " + XMLHandler.addTagValue("url", this.url));
		retval.append("    " + XMLHandler.addTagValue("userAccount", this.userAccount));
		retval.append("    " + XMLHandler.addTagValue("password", this.password));
		retval.append("    " + XMLHandler.addTagValue("ecname", this.ecname));
		retval.append("    " + XMLHandler.addTagValue("autograph", this.autograph));
		// 拼接输入输出流
		retval.append("    <fields>").append(Const.CR);
		for (int i = 0; i < this.fieldInStream.length; i++) {
			retval.append("    <field>").append(Const.CR);
			retval.append("     " + XMLHandler.addTagValue("fieldInStream", fieldInStream[i]));
			retval.append("     " + XMLHandler.addTagValue("fieldOutStream", fieldOutStream[i]));
			retval.append("    </field>").append(Const.CR);
		}
		retval.append("    </fields>").append(Const.CR);
		return retval.toString();
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		rep.saveStepAttribute(id_transformation, id_step, "url", this.url);
		rep.saveStepAttribute(id_transformation, id_step, "userAccount", this.userAccount);
		rep.saveStepAttribute(id_transformation, id_step, "password", this.password);
		rep.saveStepAttribute(id_transformation, id_step, "ecname", this.ecname);
		rep.saveStepAttribute(id_transformation, id_step, "autograph", this.autograph);

		for (int i = 0; i < this.fieldInStream.length; i++) {
			rep.saveStepAttribute(id_transformation, id_step, i, "fieldInStream", this.fieldInStream[i]);
			rep.saveStepAttribute(id_transformation, id_step, i, "fieldOutStream", this.fieldOutStream[i]);
		}
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {

		this.url = rep.getStepAttributeString(id_step, "url");
		this.userAccount = rep.getStepAttributeString(id_step, "userAccount");
		this.password = rep.getStepAttributeString(id_step, "password");
		this.ecname = rep.getStepAttributeString(id_step, "ecname");
		this.autograph = rep.getStepAttributeString(id_step, "autograph");

		int nrkeys = rep.countNrJobEntryAttributes(id_step, "fieldInStream");

		allocate(nrkeys);
		for (int i = 0; i < nrkeys; i++) {
			fieldInStream[i] = Const.NVL(rep.getStepAttributeString(id_step, i, "fieldInStream"), "");
			fieldOutStream[i] = Const.NVL(rep.getStepAttributeString(id_step, i, "fieldOutStream"), "");
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
			IMetaStore metaStore) {

		CheckResult cr;

		if (prev != null && prev.size() > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
					BaseMessages.getString(PKG, "SMS.CheckResult.StepRecevingData"), stepMeta);
			remarks.add(cr);

			// 针对输入的登录信息进行检查
			if (url != null && url.length() > 0) {
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SMS.CheckResult.url"),
						stepMeta);
				remarks.add(cr);
			} else {
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
						BaseMessages.getString(PKG, "SMS.CheckResult.urlError"), stepMeta);
				remarks.add(cr);
			}

			if (userAccount != null && userAccount.length() > 0) {
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
						BaseMessages.getString(PKG, "SMS.CheckResult.userAccount"), stepMeta);
				remarks.add(cr);
			} else {
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
						BaseMessages.getString(PKG, "SMS.CheckResult.userAccountError"), stepMeta);
				remarks.add(cr);
			}

			if (password != null && password.length() > 0) {
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
						BaseMessages.getString(PKG, "SMS.CheckResult.password"), stepMeta);
				remarks.add(cr);
			} else {
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
						BaseMessages.getString(PKG, "SMS.CheckResult.passwordError"), stepMeta);
				remarks.add(cr);
			}

			if (ecname != null && ecname.length() > 0) {
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SMS.CheckResult.ecname"),
						stepMeta);
				remarks.add(cr);
			} else {
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
						BaseMessages.getString(PKG, "SMS.CheckResult.ecnameError"), stepMeta);
				remarks.add(cr);
			}

			if (autograph != null && autograph.length() > 0) {
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
						BaseMessages.getString(PKG, "SMS.CheckResult.autograph"), stepMeta);
				remarks.add(cr);
			} else {
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
						BaseMessages.getString(PKG, "SMS.CheckResult.autographError"), stepMeta);
				remarks.add(cr);
			}
			// 针对输入输出流信息进行检查
			for (int i = 0; i < fieldInStream.length; i++) {
				if (fieldInStream[i] != null && fieldInStream[i].length() > 0) {
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
							BaseMessages.getString(PKG, "SMS.CheckResult.inputRecive"), stepMeta);
					remarks.add(cr);
				} else {
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
							BaseMessages.getString(PKG, "SMS.CheckResult.inputReciveError"), stepMeta);
					remarks.add(cr);
				}
			}
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG, "SMS.CheckResult.NotReceivingFields"), stepMeta);
			remarks.add(cr);
		}

		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
					BaseMessages.getString(PKG, "SMS.CheckResult.StepRecevingData2"), stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG, "SMS.CheckResult.NoInputReceivedFromOtherSteps"), stepMeta);
			remarks.add(cr);
		}

	}

	@Override
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
			Trans trans) {
		return new ShortMessagePlugin(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	@Override
	public StepDataInterface getStepData() {
		return new ShortMessagePluginData();
	}

	public StepDialogInterface getDialog(Shell shell, Object in, TransMeta transMeta, String name) {
		return new ShortMessagePluginDialog(shell, in, transMeta, name);
	}
}
