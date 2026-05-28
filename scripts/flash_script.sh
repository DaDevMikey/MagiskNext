#MAGISK
############################################
# Magisk Next Flash Script (updater-script)
############################################

##############
# Preparation
##############

# Default permissions
umask 022

OUTFD=$2
COMMONDIR=$INSTALLER/assets
CHROMEDIR=$INSTALLER/assets/chromeos

if [ ! -f $COMMONDIR/util_functions.sh ]; then
  echo "! Unable to extract zip file!"
  exit 1
fi

# Load utility functions
. $COMMONDIR/util_functions.sh

setup_flashable

############
# Detection
############

if echo $MAGISK_VER | grep -q '\.'; then
  PRETTY_VER=$MAGISK_VER
else
  PRETTY_VER="$MAGISK_VER($MAGISK_VER_CODE)"
fi
print_title "Magisk Next $PRETTY_VER Installer"

is_mounted /data || mount /data || is_mounted /cache || mount /cache
mount_partitions
check_data
get_flags
find_boot_image

[ -z $BOOTIMAGE ] && abort "! Unable to detect target image"
ui_print "- Target image: $BOOTIMAGE"

# Detect version and architecture
api_level_arch_detect

[ $API -lt 23 ] && abort "! Magisk Next only supports Android 6.0 and above"

ui_print "- Device platform: $ABI"

BINDIR=$INSTALLER/lib/$ABI
cd $BINDIR
for file in lib*.so; do mv "$file" "${file:3:${#file}-6}"; done
cd /
cp -af $INSTALLER/lib/$ABI32/libmagisk.so $BINDIR/magisk32 2>/dev/null

# Check if system root is installed and remove
$BOOTMODE || remove_system_su

##############
# Environment
##############

ui_print "- Constructing environment"

# Copy required files
rm -rf $MAGISKBIN 2>/dev/null
mkdir -p $MAGISKBIN 2>/dev/null
cp -af $BINDIR/. $COMMONDIR/. $BBBIN $MAGISKBIN

# Remove files only used by the Magisk app
rm -f $MAGISKBIN/bootctl $MAGISKBIN/main.jar \
  $MAGISKBIN/module_installer.sh $MAGISKBIN/uninstaller.sh

chmod -R 755 $MAGISKBIN

# addon.d
if [ -d /system/addon.d ]; then
  ui_print "- Adding addon.d survival script"
  blockdev --setrw /dev/block/mapper/system$SLOT 2>/dev/null
  mount -o rw,remount /system || mount -o rw,remount /
  ADDOND=/system/addon.d/99-magisk.sh
  cp -af $COMMONDIR/addon.d.sh $ADDOND
  chmod 755 $ADDOND
fi

##################
# Image Patching
##################

install_magisk

# Inject Bootloop Protector
ui_print "- Installing Bootloop Protector"
mkdir -p /data/adb/post-fs-data.d 2>/dev/null
cat << 'EOF' > /data/adb/post-fs-data.d/bootloop_protector.sh
#!/system/bin/sh
# Magisk Next Bootloop Protector
COUNT_FILE="/data/adb/magisk/boot_count"

if [ -f "$COUNT_FILE" ]; then
    COUNT=$(cat "$COUNT_FILE")
    COUNT=$((COUNT + 1))
else
    COUNT=1
fi

echo "$COUNT" > "$COUNT_FILE"

if [ "$COUNT" -ge 3 ]; then
    # Bootloop detected
    echo "Bootloop detected! Disabling all modules..." > /data/adb/magisk/bootloop_log.txt
    for MOD in /data/adb/modules/*; do
        touch "$MOD/disable"
    done
    rm -f "$COUNT_FILE"
fi
EOF
chmod 755 /data/adb/post-fs-data.d/bootloop_protector.sh

# A late_start service to reset the counter once boot succeeds
mkdir -p /data/adb/service.d 2>/dev/null
cat << 'EOF' > /data/adb/service.d/bootloop_reset.sh
#!/system/bin/sh
# Wait until sys.boot_completed
until [ "$(getprop sys.boot_completed)" = "1" ]; do
    sleep 2
done
rm -f /data/adb/magisk/boot_count
EOF
chmod 755 /data/adb/service.d/bootloop_reset.sh

# Cleanups
$BOOTMODE || recovery_cleanup
rm -rf $TMPDIR

ui_print "- Done"
exit 0
