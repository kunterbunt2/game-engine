/*
 * Copyright (C) 2024 Abdalla Bushnaq
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bushnaq.abdalla.engine.audio.synthesis.util;

import de.bushnaq.abdalla.engine.IContextFactory;

/**
 * @author kunterbunt
 */
public class BasicDesktopContextFactory implements IContextFactory {
    private Context context;

    @Override
    public Context create() {
        if (context == null) {
            context = new BasicDesktopContext();
        }
        return getContext();
    }

    Context getContext() {
        return context;
    }

}
