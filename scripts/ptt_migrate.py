import json
import os
from datetime import datetime
from nbt.nbt import NBTFile, TAG_Compound, TAG_Long, TAG_Int, TAG_String

dt = datetime.now()
year_id = dt.year
month_id = dt.year * 12 + dt.month - 1
week_id = dt.year * 366 + dt.timetuple().tm_yday - dt.weekday() - 1
day_id = dt.year * 366 + dt.timetuple().tm_yday


def add_track_data(data, name, period_id, duration):
    item = TAG_Compound()
    item['duration'] = TAG_Long(duration)
    item['periodId'] = TAG_Int(period_id)
    item['status'] = TAG_String('NOT_MEET')
    data[name] = item


def process(uuid):
    stat_file = os.path.join('world/stats', uuid + '.json')
    nbt_file = os.path.join('world/imyvm_essential', uuid + '.dat')

    with open(stat_file) as f:
        stat = json.load(f)

    if 'minecraft:play_time_track' not in stat['stats']['minecraft:custom']:
        return
    ptt = stat['stats']['minecraft:custom']['minecraft:play_time_track'] * 1000

    del stat['stats']['minecraft:custom']['minecraft:play_time_track']
    with open(stat_file, 'w') as f:
        json.dump(stat, f, separators=(',', ':'))

    try:
        nbt = NBTFile(nbt_file)
    except FileNotFoundError:
        nbt = NBTFile()

    data = TAG_Compound()
    nbt['playerTrackData'] = data

    # *******************************************************
    # |  IMPORTANT: please choose what you want to migrate  |
    # *******************************************************
    add_track_data(data, 'total', 1, ptt)
    # add_track_data(data, 'year', year_id, ptt)
    # add_track_data(data, 'month', month_id, ptt)
    # add_track_data(data, 'week', week_id, ptt)
    # add_track_data(data, 'day', day_id, ptt)

    nbt.write_file(nbt_file)


def main():
    for file in os.listdir('world/stats'):
        uuid = os.path.splitext(file)[0]
        process(uuid)


if __name__ == '__main__':
    main()
