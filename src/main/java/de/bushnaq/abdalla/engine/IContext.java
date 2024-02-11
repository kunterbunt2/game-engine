package de.bushnaq.abdalla.engine;

import de.bushnaq.abdalla.engine.IApplicationProperties;

/**
 * @author kunterbunt
 *
 */
public interface IContext extends IApplicationProperties {

	void disableClipping();

	void enableClipping();

	float getWaterLevel();

}