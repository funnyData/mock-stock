package com.longone.broker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

/**
 * Created by IntelliJ IDEA.
 * User: figo
 * Date: 2010-11-7
 * Time: 20:37:56
 * To change this template use File | Settings | File Templates.
 */
public class PasswordResetPanel extends VerticalPanel {
    Button submitButton = new Button("提交");
    PasswordTextBox password1 = new PasswordTextBox();
    PasswordTextBox password2 = new PasswordTextBox();

    public PasswordResetPanel(final StockServiceAsync stockPriceSvc) {
        HorizontalPanel panel1 = new HorizontalPanel();
        panel1.add(new Label("新的密码:"));
        panel1.add(password1);

        HorizontalPanel panel2 = new HorizontalPanel();
        panel2.add(new Label("再输一次:"));
        panel2.add(password2);

        password1.setFocus(true);

        this.add(panel1);
        this.add(panel2);
        this.add(submitButton);

        submitButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                String pwd1 = password1.getText().trim();
                String pwd2 = password2.getText().trim();
                submitButton.setEnabled(false);
                if (!pwd1.equals(pwd2)) {
                    Window.alert("密码不一致，请重新输入");
                    resetFields();
                } else {
                    // Set up the callback object.
                    AsyncCallback<String> callback = new AsyncCallback<String>() {
                        public void onFailure(Throwable caught) {
                            // TODO: Do something with errors.
                            GWT.log(caught.toString());
                        }

                        public void onSuccess(String s) {
                            Window.alert(s);
                            resetFields();
                        }
                    };
                    // Make the call to the stock price service.
                    stockPriceSvc.resetPassword(pwd1, callback);
                }
            }
        });

    }

    private void resetFields() {
        password1.setText("");
        password2.setText("");
        password1.setFocus(true);
        submitButton.setEnabled(true);
    }
}
