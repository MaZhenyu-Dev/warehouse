import { describe, it, expect } from 'vitest';
import { getNthWeekday, getUSHolidays, getHour } from '../timeHelpers';

describe('timeHelpers', () => {
  it('getNthWeekday calculates correct date', () => {
    // Thanksgiving 2023: Nov 23 (4th Thursday of Nov)
    // Month is 0-indexed, so 10 is November
    // Weekday: 0=Sun, 4=Thu
    const thanksgiving = getNthWeekday(2023, 10, 4, 4);
    expect(thanksgiving.getFullYear()).toBe(2023);
    expect(thanksgiving.getMonth()).toBe(10);
    expect(thanksgiving.getDate()).toBe(23);
  });

  it('getUSHolidays returns holidays sorted by date', () => {
    const holidays = getUSHolidays(2023);
    expect(holidays.length).toBeGreaterThan(0);
    for (let i = 0; i < holidays.length - 1; i++) {
      expect(holidays[i].date.getTime()).toBeLessThanOrEqual(holidays[i + 1].date.getTime());
    }
  });

  it('getHour returns correct hour for timezone', () => {
    const date = new Date('2023-01-01T12:00:00Z');
    // UTC 12:00 is America/New_York 07:00 (EST is UTC-5)
    const hour = getHour(date, 'America/New_York');
    expect(hour).toBe(7);
  });
});
