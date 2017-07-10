package org.pentaho.di.trans.steps.shortmessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.shortmessage.ShortMessagePluginMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

public class ShortMessagePluginDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = ShortMessagePluginDialog.class;

	private ShortMessagePluginMeta input;
	private ColumnInfo[] colinf;
	private Map<String, Integer> inputFields;

	// 第一个Label,写入登录url信息
	private Label wlUrl;
	private Text wUrl;
	private FormData fdlUrl, fdUrl;

	// 第二个Label,写入登录用户信息
	private Label wlUser;
	private Text wUser;
	private FormData fdlUser, fdUser;

	// 第三个Label,写入登录密码信息
	private Label wlPass;
	private Text wPass;
	private FormData fdlPass, fdPass;

	// 第四个Label,写入登录企业信息
	private Label wlEcname;
	private Text wEcname;
	private FormData fdlEcname, fdEcname;

	// 第五个Label，写入短信签名ID
	private Label wlAutograph;
	private Text wAutograph;
	private FormData fdlAutograph, fdAutograph;

	// 第五个表格是一个群状的，用于存储获得的数据库信息
	private Label wlMessage;
	private TableView wMessage;
	private FormData fdlMessage, fdMessage;

	public ShortMessagePluginDialog(Shell parent, Object in, TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta) in, transMeta, stepname);
		input = (ShortMessagePluginMeta) in;
		inputFields = new HashMap<String, Integer>();
	}

	@Override
	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		// 进行窗口处各个表格的设计
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "ShortMessagePluginDialog.Shell.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// shell窗口内表格的形状设计
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "ShortMessageDialog.StepName.Label"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// 输入登陆账号的表格位置,需要5个Label
		// 第一个：
		wlUrl = new Label(shell, SWT.RIGHT);
		wlUrl.setText(BaseMessages.getString(PKG, "ShortMessageDialog.Url.Label"));
		props.setLook(wlUrl);
		fdlUrl = new FormData();
		fdlUrl.left = new FormAttachment(0, 0);
		fdlUrl.right = new FormAttachment(middle, -margin);
		fdlUrl.top = new FormAttachment(wStepname, margin);
		wlUrl.setLayoutData(fdlUrl);

		wUrl = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wUrl.setText("");
		props.setLook(wUrl);
		wUrl.addModifyListener(lsMod);
		fdUrl = new FormData();
		fdUrl.left = new FormAttachment(middle, 0);
		fdUrl.right = new FormAttachment(100, 0);
		fdUrl.top = new FormAttachment(wStepname, margin);
		wUrl.setLayoutData(fdUrl);

		// 第二个：
		wlUser = new Label(shell, SWT.RIGHT);
		wlUser.setText(BaseMessages.getString(PKG, "ShortMessageDialog.Account.Label"));
		props.setLook(wlUser);
		fdlUser = new FormData();
		fdlUser.left = new FormAttachment(0, 0);
		fdlUser.right = new FormAttachment(middle, -margin);
		fdlUser.top = new FormAttachment(wUrl, margin);
		wlUser.setLayoutData(fdlUser);

		wUser = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wUser.setText("");
		props.setLook(wUser);
		wUser.addModifyListener(lsMod);
		fdUser = new FormData();
		fdUser.left = new FormAttachment(middle, 0);
		fdUser.right = new FormAttachment(100, 0);
		fdUser.top = new FormAttachment(wUrl, margin);
		wUser.setLayoutData(fdUser);

		// 第三个
		wlPass = new Label(shell, SWT.RIGHT);
		wlPass.setText(BaseMessages.getString(PKG, "ShortMessageDialog.Password.Label"));
		props.setLook(wlPass);
		fdlPass = new FormData();
		fdlPass.left = new FormAttachment(0, 0);
		fdlPass.right = new FormAttachment(middle, -margin);
		fdlPass.top = new FormAttachment(wUser, margin);
		wlPass.setLayoutData(fdlPass);

		wPass = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wPass.setText("");
		props.setLook(wPass);
		wPass.addModifyListener(lsMod);
		fdPass = new FormData();
		fdPass.left = new FormAttachment(middle, 0);
		fdPass.right = new FormAttachment(100, 0);
		fdPass.top = new FormAttachment(wUser, margin);
		wPass.setLayoutData(fdPass);

		// 第四个：
		wlEcname = new Label(shell, SWT.RIGHT);
		wlEcname.setText(BaseMessages.getString(PKG, "ShortMessageDialog.Ecname.Label"));
		props.setLook(wlEcname);
		fdlEcname = new FormData();
		fdlEcname.left = new FormAttachment(0, 0);
		fdlEcname.right = new FormAttachment(middle, -margin);
		fdlEcname.top = new FormAttachment(wPass, margin);
		wlEcname.setLayoutData(fdlEcname);

		wEcname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wEcname.setText("");
		props.setLook(wEcname);
		wEcname.addModifyListener(lsMod);
		fdEcname = new FormData();
		fdEcname.left = new FormAttachment(middle, 0);
		fdEcname.right = new FormAttachment(100, 0);
		fdEcname.top = new FormAttachment(wPass, margin);
		wEcname.setLayoutData(fdEcname);

		// 第五个
		wlAutograph = new Label(shell, SWT.RIGHT);
		wlAutograph.setText(BaseMessages.getString(PKG, "ShortMessageDialog.Autograph.Label"));
		props.setLook(wlAutograph);
		fdlAutograph = new FormData();
		fdlAutograph.left = new FormAttachment(0, 0);
		fdlAutograph.right = new FormAttachment(middle, -margin);
		fdlAutograph.top = new FormAttachment(wEcname, margin);
		wlAutograph.setLayoutData(fdlAutograph);

		wAutograph = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wAutograph.setText("");
		props.setLook(wAutograph);
		wAutograph.addModifyListener(lsMod);
		fdAutograph = new FormData();
		fdAutograph.left = new FormAttachment(middle, 0);
		fdAutograph.right = new FormAttachment(100, 0);
		fdAutograph.top = new FormAttachment(wEcname, margin);
		wAutograph.setLayoutData(fdAutograph);

		// 获取数据的表格
		wlMessage = new Label(shell, SWT.NONE);
		wlMessage.setText(BaseMessages.getString(PKG, "ShortMessageDialog.Data.Label"));
		props.setLook(wlMessage);
		fdlMessage = new FormData();
		fdlMessage.left = new FormAttachment(0, 0);
		fdlMessage.top = new FormAttachment(wAutograph, margin);
		wlMessage.setLayoutData(fdlMessage);

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "ShortMessageDialog.ColumInfo.Button"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		setButtonPositions(new Button[] { wOK, wCancel, wGet }, margin, null);

		final int fieldsCols = 2;
		final int fieldRows = (input.getFieldInStream() != null ? input.getFieldInStream().length : 1);

		colinf = new ColumnInfo[fieldsCols];
		colinf[0] = new ColumnInfo(BaseMessages.getString(PKG, "InStream"), ColumnInfo.COLUMN_TYPE_CCOMBO,
				new String[] { "" }, false);
		colinf[1] = new ColumnInfo(BaseMessages.getString(PKG, "OutStream"), ColumnInfo.COLUMN_TYPE_CCOMBO,
				new String[] { ""}, false);
		wMessage = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, fieldRows,
				lsMod, props);

		fdMessage = new FormData();
		fdMessage.left = new FormAttachment(0, 0);
		fdMessage.top = new FormAttachment(wlMessage, margin);
		fdMessage.right = new FormAttachment(100, 0);
		fdMessage.bottom = new FormAttachment(wGet, -2 * margin);
		wMessage.setLayoutData(fdMessage);

		// 后台实现查询数据
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				StepMeta stepMeta = transMeta.findStep(stepname);
				if (stepMeta != null) {
					try {
						RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
						for (int i = 0; i < row.size(); i++) {
							inputFields.put(row.getValueMeta(i).getName(), i);
						}
						setComboBoxes();
					} catch (KettleStepException e) {
						logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
					}
				}
			}
		};
		new Thread(runnable).start();

		// 增加监听器
		lsOK = new Listener() {
			@Override
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsCancel = new Listener() {
			@Override
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsGet = new Listener() {
			@Override
			public void handleEvent(Event e) {
				get();
			}
		};
		wOK.addListener(SWT.Selection, lsOK);
		wGet.addListener(SWT.Selection, lsGet);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		setSize();
		getData();

		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return stepname;
	}

	protected void setComboBoxes() {
		final Map<String, Integer> fields = new HashMap<String, Integer>();

		fields.putAll(inputFields);
		Set<String> keySet = fields.keySet();
		List<String> entries = new ArrayList<String>(keySet);

		String[] fieldNames = entries.toArray(new String[entries.size()]);
		Const.sortStrings(fieldNames);

		colinf[0].setComboValues(fieldNames);
	}

	public void getData() {

		if (input.getUrl() != null) {
			wUrl.setText(input.getUrl());
		}
		if (input.getUserAccount() != null) {
			wUser.setText(input.getUserAccount());
		}
		if (input.getPassword() != null) {
			wPass.setText(input.getPassword());
		}
		if (input.getEcname() != null) {
			wEcname.setText(input.getEcname());
		}

		for (int i = 0; i < input.getFieldInStream().length; i++) {
			TableItem item = wMessage.table.getItem(i);
			if (input.getFieldInStream()[i] != null) {
				item.setText(1, input.getFieldInStream()[i]);
			}
			if (input.getFieldOutStream()[i] != null) {
				item.setText(1, input.getFieldOutStream()[i]);
			}
		}

		wMessage.setRowNums();
		wMessage.optWidth(true);

		wStepname.selectAll();
		wStepname.setFocus();
	}

	private void ok() {
		if (Utils.isEmpty(wStepname.getText())) {
			return;
		}
		int nrkeys = wMessage.nrNonEmpty();
		input.allocate(nrkeys);

		if (log.isDebug()) {
			logDebug(BaseMessages.getString(PKG, "SMS.Log.FoundFields", String.valueOf(nrkeys)));
		}

		// 将配置的数据复制到meta类中
		stepname = wStepname.getText();
		input.setUrl(wUrl.getText());
		input.setUserAccount(wUser.getText());
		input.setPassword(wPass.getText());
		input.setEcname(wEcname.getText());
		input.setAutograph(wAutograph.getText());
		for (int i = 0; i < nrkeys; i++) {
			TableItem item = wMessage.getNonEmpty(i);
			input.getFieldInStream()[i] = item.getText(1);
			input.getFieldOutStream()[i] = item.getText(2);
		}
		dispose();
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void get() {
		try {
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null && !r.isEmpty()) {
				TableItemInsertListener listener = new TableItemInsertListener() {
					public boolean tableItemInserted(TableItem arg0, ValueMetaInterface arg1) {
						return true;
					}
				};
				BaseStepDialog.getFieldsFromPrevious(r, wMessage, 1, new int[] { 1 }, new int[] {}, -1, -1, listener);
			}
		} catch (KettleStepException e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "SMS.FailedToGetFields.DialogTitle"),
					BaseMessages.getString(PKG, "SMS.FailedToGetFields.DialogMessage"), e);
		}
	}
}
