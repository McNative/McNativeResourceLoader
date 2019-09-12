/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 27.08.19, 18:03
 *
 * The McNative Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.mcnative.service.entity.living.animal.horse;

import org.mcnative.service.entity.living.animal.Animal;
import org.mcnative.service.entity.living.animal.Tameable;
import org.mcnative.service.entity.vehicle.Vehicle;
import org.mcnative.service.inventory.InventoryOwner;
import org.mcnative.service.inventory.type.HorseInventory;

public interface AbstractHorse extends Animal, Vehicle, InventoryOwner, Tameable {

    int getDomestication();

    void setDomestication(int level);

    int getMaxDomestication();

    void setMaxDomestication(int level);

    double getJumpStrength();

    void setJumpStrength(double strength);

    HorseInventory getInventory();
}