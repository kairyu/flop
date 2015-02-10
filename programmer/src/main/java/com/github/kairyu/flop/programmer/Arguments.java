/*
 * Copyright (C) 2015  Kai Ryu <kai1103@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.kairyu.flop.programmer;

import com.github.kairyu.flop.programmer.atmel.DeviceType;
import com.github.kairyu.flop.programmer.atmel.MemoryUnit;
import com.github.kairyu.flop.programmer.atmel.Target;
import com.github.kairyu.flop.programmer.command.Command;
import com.github.kairyu.flop.programmer.command.Configure;
import com.github.kairyu.flop.programmer.command.Get;
import com.github.kairyu.flop.programmer.command.GetFuse;
import com.github.kairyu.flop.programmer.command.SetFuse;
import com.github.kairyu.flop.programmer.exception.UnsupportedTargetException;

import java.util.Map;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import net.sourceforge.argparse4j.internal.HelpScreenException;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;
import static net.sourceforge.argparse4j.impl.Arguments.storeConst;
import static net.sourceforge.argparse4j.impl.Arguments.fileType;
import static net.sourceforge.argparse4j.impl.Arguments.version;
import static net.sourceforge.argparse4j.impl.Arguments.help;
import static net.sourceforge.argparse4j.impl.Arguments.SUPPRESS;

/**
 * @author Kai Ryu
 *
 */
public class Arguments {

    private ArgumentParser parser;
    private Namespace ns;
    private boolean quiet;
    private int debug;
    private Target target;
    private Command command;

    public boolean getQuiet() {
        return this.quiet;
    }

    public int getDebug() {
        return this.debug;
    }

    public Command getCommand() {
        return this.command;
    }

    public String getCommandName() {
        return this.command.getName();
    }

    public Target getTarget() {
        return this.target;
    }

    public String getTargetName() {
        return this.target.getName();
    }

    public DeviceType getDeviceType() {
        return this.target.getDeviceType();
    }

    public String getDeviceTypeName() {
        return this.target.getDeviceType().getName();
    }

    public int getVendorId() {
        return this.target.getVendorId();
    }

    public int getProductId() {
        return this.target.getChipId();
    }

    public int getChipId() {
        return this.target.getChipId();
    }

    public boolean getInitialAbort() {
        return this.target.getInitialAbort();
    }

    public boolean getHonorInterfaceClass() {
        //return this.target.getHonorInterfaceClass();
        return false;
    }

    public int getEepromMemorySize() {
        return this.target.getEepromMemorySize();
    }

    public int getFlashPageSize() {
        return this.target.getFlashPageSize();
    }

    public int getEepromPageSize() {
        return this.target.getEepromPageSize();
    }

    public int getMemoryAddressTop() {
        return this.target.getMemoryAddressTop();
    }

    public int getMemoryAddressBottom() {
        return this.target.getMemoryAddressBottom();
    }

    public int getFlashAddressTop() {
        return this.target.getFlashAddressTop();
    }

    public int getFlashAddressBottom() {
        return this.target.getFlashAddressBottom();
    }

    public int getBootloaderTop() {
        return this.target.getBootloaderTop();
    }

    public int getBootloaderBottom() {
        return this.target.getBootloaderBottom();
    }

    public String getConfigureName() {
        return this.ns.getString("name");
    }

    public boolean getConfigureSuppressValidation() {
        return this.ns.getBoolean("suppress_validation");
    }

    public int getConfigureValue() {
        return this.ns.getInt("value");
    }

    // TODO: implement getSetFuse

    public boolean getReadBin() {
        return this.ns.getBoolean("bin");
    }

    public boolean getReadForce() {
        return this.ns.getBoolean("force");
    }

    public MemoryUnit getReadSegment() {
        return MemoryUnit.valueOf(this.ns.getString("segment"));
    }

    public boolean getEraseForce() {
        return this.ns.getBoolean("force");
    }

    public boolean getEraseSuppressValidation() {
        return this.ns.getBoolean("suppress_validation");
    }

    public MemoryUnit getFlashSegment() {
        return MemoryUnit.valueOf(this.ns.getString("segment"));
    }

    public boolean getLaunchNoReset() {
        return this.ns.getBoolean("no_reset");
    }

    public boolean getFlashSuppressValidation() {
        return this.ns.getBoolean("suppress_validation");
    }

    public boolean getFlashSuppressBootloaderMem() {
        return this.ns.getBoolean("suppress_bootloader_mem");
    }

    public String getFlashFileName() {
        return this.ns.getString("file");
    }

    public boolean getFlashForce() {
        return this.ns.getBoolean("force");
    }

    public Get getGetName() {
        return Get.getByName(this.ns.getString("name"));
    }

    public Arguments() {
        this.parser = ArgumentParsers.newArgumentParser("flop", false)
                .version("${prog} 0.1.0");

        MutuallyExclusiveGroup helpGroup = this.parser.addMutuallyExclusiveGroup();

        helpGroup.addArgument("-h", "--help")
                .help("show a list of commands")
                .action(help())
                .setDefault(SUPPRESS);
        helpGroup.addArgument("-t", "--targets")
                .help("list supported target devices")
                .action(new ArgumentAction() {
                    @Override
                    public void run(ArgumentParser parser, Argument arg,
                                    Map<String, Object> attrs, String flag, Object value)
                            throws ArgumentParserException {
                        Arguments.printTargets();
                        throw new HelpScreenException(parser);
                    }

                    @Override
                    public void onAttach(Argument arg) {
                    }

                    @Override
                    public boolean consumeArgument() {
                        return false;
                    }
                })
                .setDefault(SUPPRESS);
        helpGroup.addArgument("-v", "--version")
                .help("show version information")
                .action(version())
                .setDefault(SUPPRESS);

        ArgumentGroup global = this.parser.addArgumentGroup("global options");
        global.addArgument("-q", "--quiet")
                .action(storeTrue())
                .dest("quiet");
        global.addArgument("-d", "--debug")
                .type(Integer.class)
                .metavar("level")
                .help("(level is an integer specifying level of detail)")
                .setDefault(0);

        this.parser.addArgument("target")
                .type(new ArgumentType<Target>() {
                    @Override
                    public Target convert(ArgumentParser parser, Argument arg, String value)
                            throws ArgumentParserException {
                        try {
                            return Target.getByName(value);
                        } catch (IllegalArgumentException e) {
                            throw new UnsupportedTargetException(String.format(
                                    "unsupported target '%s'", value), parser, value);
                        }
                    }
                });

        Subparsers commands = this.parser.addSubparsers()
                .title("command summary")
                .metavar("command")
                .dest("command");

        Subparser launch = commands.addParser("launch")
                .help("launch from the bootloader into the main program using a watchdog reset");
        launch.addArgument("--no-reset")
                .action(storeTrue())
                .help("jump directly into the main program");

        Subparser read = commands.addParser("read")
                .help("read the program memory in flash and output non-blank pages in ihex format");
        read.addArgument("-f", "--force")
                .action(storeTrue())
                .help("output the entire memory");
        read.addArgument("-b", "--bin")
                .action(storeTrue())
                .help("binary output");
        read.setDefault("segment", MemoryUnit.flash);
        MutuallyExclusiveGroup readSegment = read.addMutuallyExclusiveGroup("memory segment");
        readSegment.addArgument("--flash")
                .type(MemoryUnit.class)
                .action(storeConst())
                .setConst(MemoryUnit.flash)
                .dest("segment")
                .help("default");
        readSegment.addArgument("--user")
                .type(MemoryUnit.class)
                .action(storeConst())
                .setConst(MemoryUnit.user)
                .dest("segment")
                .help("select user page");
        readSegment.addArgument("--eeprom")
                .type(MemoryUnit.class)
                .action(storeConst())
                .setConst(MemoryUnit.eeprom)
                .dest("segment")
                .help("select eeprom");

        Subparser erase = commands.addParser("erase")
                .help("erase memory contents if the chip is not blank");
        erase.addArgument("-f", "--force")
                .action(storeTrue())
                .help("always erase even the chip is blank");
        erase.addArgument("-V", "--suppress-validation")
                .action(storeTrue());

        Subparser flash = commands.addParser("flash")
                .help("flash a program onto device flash memory");
        flash.addArgument("-f", "--force")
                .action(storeTrue())
                .help("ignore warning when data exists in target memory region\n"
                    + "bootloader configuration uses last 4 to 8 bytes of user page, "
                    + "--force always required here");
        flash.setDefault("segment", MemoryUnit.flash);
        MutuallyExclusiveGroup flashSegment = flash.addMutuallyExclusiveGroup("memory segment");
        flashSegment.addArgument("--flash")
                .action(storeConst())
                .setConst(MemoryUnit.flash)
                .dest("segment")
                .help("default");
        flashSegment.addArgument("--user")
                .action(storeConst())
                .setConst(MemoryUnit.user)
                .dest("segment")
                .help("select user page");
        flashSegment.addArgument("--eeprom")
                .action(storeConst())
                .setConst(MemoryUnit.eeprom)
                .dest("segment")
                .help("select eeprom");
        flash.addArgument("-V", "--suppress-validation")
                .action(storeTrue());
        flash.addArgument("-B", "--suppress-bootloader-mem")
                .action(storeTrue());
        flash.addArgument("-s", "--serial")
                .metavar("hexdigits:offset");
        flash.addArgument("file")
                .type(fileType().acceptSystemIn().verifyCanRead())
                .setDefault("-");

        Subparser setsecure = commands.addParser("setsecure")
                .help(" ");
        setsecure.addArgument("");

        Subparser configure = commands.addParser("configure")
                .help(" ");
        configure.addArgument("name")
                .type(Configure.class)
                .choices(Configure.values());
        configure.addArgument("-V", "--suppress-validation")
                .action(storeTrue());
        configure.addArgument("value")
                .type(String.class);

        Subparser get = commands.addParser("get")
                .help(" ");
        get.setDefault("name", Get.bootloader);
        get.addArgument("name")
                .type(new ArgumentType<Get>() {
                    @Override
                    public Get convert(ArgumentParser parser, Argument arg, String value)
                            throws ArgumentParserException {
                        try {
                            return Get.getByName(value);
                        }
                        catch (IllegalArgumentException e) {
                            throw new ArgumentParserException(String.format(
                                    "invalid choice: '%s' (choose from %s)",
                                    value,
                                    Get.textualFormat()),
                                    parser);
                        }
                    }
                })
                .choices(Get.values())
                .nargs("?");

        Subparser getfuse = commands.addParser("getfuse")
                .help(" ");
        getfuse.addArgument("name")
                .type(GetFuse.class)
                .choices(GetFuse.values());

        Subparser setfuse = commands.addParser("setfuse")
                .help(" ");
        setfuse.addArgument("name")
                .type(SetFuse.class)
                .choices(SetFuse.values());
        setfuse.addArgument("data")
                .type(String.class);
    }

    public int parseArguments(final String[] args) {
        try {
            this.ns = this.parser.parseArgs(args);
        }
        catch (ArgumentParserException e) {
            this.parser.handleError(e);
            if (e instanceof HelpScreenException) {
                return 1;
            }
            if (e instanceof UnsupportedTargetException) {
                printTargets();
            }
            return -1;
        }

        this.command = Command.getByName(ns.getString("command"));
        this.assignTarget(ns);
        this.assignGlobalOptions(ns);
        this.assignCommandOptions(ns);

        if (this.debug > 1) {
            System.out.println(ns);
            this.printArgs();
        }
        return 0;
    }

    private void assignTarget(final Namespace ns) {
        this.target = Target.getByName(ns.getString("target"));
    }

    private void assignGlobalOptions(final Namespace ns) {
        this.quiet = ns.getBoolean("quiet");
        this.debug = ns.getInt("debug");
    }

    private void assignCommandOptions(final Namespace ns) {
    }

    private void printArgs() {
        System.err.println(String.format("     target: %s", this.getTargetName()));
        System.err.println(String.format("    chip_id: 0x%04x", this.getChipId()));
        System.err.println(String.format("  vendor_id: 0x%04x", this.getVendorId()));
        System.err.println(String.format("    command: %s", this.getCommandName()));
        System.err.println(String.format("      debug: %d", this.getDebug()));
        System.err.println(String.format("device_type: %s", this.getDeviceTypeName()));
        System.err.println(String.format("------ command specific below ------"));

        switch (this.command) {
            case configure:
                System.err.println(String.format("       name: %s", this.getConfigureName()));
                System.err.println(String.format("   validate: %b", this.getConfigureSuppressValidation()));
                System.err.println(String.format("      value: %d", this.getConfigureValue()));
                break;
            case erase:
                System.err.println(String.format("   validate: %b", this.getEraseSuppressValidation()));
                break;
            case flash:
            case eflash:
            case user:
                System.err.println(String.format("   validate: %b", this.getFlashSuppressValidation()));
                System.err.println(String.format("   hex file: %s", this.getFlashFileName()));
                break;
            case get:
                System.err.println(String.format("       name: %s", this.getGetName()));
                break;
            case launch:
                System.err.println(String.format("   no-reset: %b", this.getLaunchNoReset()));
            default:
                break;
        }
        System.err.println();
    }

    private static void printTargets() {
        for (DeviceType deviceType: DeviceType.values()) {
            if (deviceType != DeviceType.NULL) {
                System.out.println(deviceType.getName() + " based controllers:");
                int col = 0;
                for (Target target: Target.values()) {
                    if (target.getDeviceType() == deviceType) {
                        System.out.printf("  %-16s", target.getName());
                        if (++col == 4) {
                            System.out.println();
                            col = 0;
                        }
                    }
                }
                if (col != 0) {
                    System.out.println();
                }
            }
        }
    }

}
